from fastapi import FastAPI, File, UploadFile
import shutil
import os
import cv2
import pytesseract
import re
import requests
from PIL import Image, ImageChops, ImageEnhance
from pdf2image import convert_from_path
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

POPPLER_PATH = os.environ.get("POPPLER_PATH", r"C:\poppler\Library\bin")

app = FastAPI()

pytesseract.pytesseract.tesseract_cmd = r"C:\Program Files\Tesseract-OCR\tesseract.exe"

ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".pdf"}
MAX_FILE_SIZE_MB = 10
SPRING_BOOT_BASE_URL = "http://localhost:8080"


def is_allowed_file(filename: str) -> bool:
    ext = os.path.splitext(filename)[1].lower()
    return ext in ALLOWED_EXTENSIONS


def convert_pdf_to_image(pdf_path: str) -> str:
    pages = convert_from_path(pdf_path, poppler_path=POPPLER_PATH, first_page=1, last_page=1)
    image_path = pdf_path.rsplit(".", 1)[0] + "_converted.jpg"
    pages[0].save(image_path, "JPEG")
    return image_path


def extract_text_from_file(file_path: str) -> str:
    if file_path.lower().endswith(".pdf"):
        file_path = convert_pdf_to_image(file_path)

    image = cv2.imread(file_path)
    if image is None:
        return ""

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    text = pytesseract.image_to_string(gray)
    return text


def extract_roll_number(text: str):
    match = re.search(r"\d{2}[A-Za-z]{2,5}\d{3,6}", text, re.IGNORECASE)
    if match:
        return match.group().upper()
    return None


AUTH_TOKEN_CACHE = {"token": None}
AI_SERVICE_EMAIL = os.environ.get("AI_SERVICE_EMAIL", "ai-service@campusos.local")
AI_SERVICE_PASSWORD = os.environ.get("AI_SERVICE_PASSWORD", "")


def get_auth_token():
    if AUTH_TOKEN_CACHE["token"]:
        return AUTH_TOKEN_CACHE["token"]
    try:
        url = f"{SPRING_BOOT_BASE_URL}/api/auth/login"
        response = requests.post(url, json={
            "email": AI_SERVICE_EMAIL,
            "password": AI_SERVICE_PASSWORD
        }, timeout=5)
        if response.status_code == 200:
            token = response.json().get("token")
            AUTH_TOKEN_CACHE["token"] = token
            return token
        else:
            return None
    except requests.exceptions.RequestException:
        return None


def get_student_record(roll_number: str):
    token = get_auth_token()
    if not token:
        return None
    try:
        url = f"{SPRING_BOOT_BASE_URL}/api/students/{roll_number}"
        headers = {"Authorization": f"Bearer {token}"}
        response = requests.get(url, headers=headers, timeout=5)
        if response.status_code == 200:
            return response.json()
        else:
            return None
    except requests.exceptions.RequestException:
        return None


def compare_fields(ocr_text: str, db_record: dict):
    result = {}
    db_name = db_record.get("name", "")
    result["name_match"] = db_name.upper() in ocr_text.upper()
    db_roll = db_record.get("rollNumber", "")
    result["roll_number_match"] = db_roll in ocr_text
    if result["name_match"] and result["roll_number_match"]:
        result["overall_status"] = "verified"
    elif result["roll_number_match"]:
        result["overall_status"] = "partial match - name mismatch"
    else:
        result["overall_status"] = "not verified"
    return result


def calculate_similarity(text1: str, text2: str) -> float:
    if not text1.strip() or not text2.strip():
        return 0.0
    vectorizer = TfidfVectorizer()
    tfidf_matrix = vectorizer.fit_transform([text1, text2])
    similarity_score = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:2])
    return round(float(similarity_score[0][0]), 4)


@app.get("/")
def read_root():
    backend_status = "unreachable"
    try:
        response = requests.get(f"{SPRING_BOOT_BASE_URL}/api/auth/login", timeout=3)
        backend_status = "reachable"
    except requests.exceptions.RequestException:
        backend_status = "unreachable"
    return {
        "message": "CampusOS AI service is running!",
        "backend_status": backend_status
    }


@app.post("/upload-document")
def upload_document(file: UploadFile = File(...)):
    if not is_allowed_file(file.filename):
        return {"error": "Unsupported file type. Please upload a JPG, PNG, or PDF."}
    file.file.seek(0, os.SEEK_END)
    file_size_mb = file.file.tell() / (1024 * 1024)
    file.file.seek(0)
    if file_size_mb > MAX_FILE_SIZE_MB:
        return {"error": f"File too large. Maximum allowed size is {MAX_FILE_SIZE_MB}MB."}
    os.makedirs("uploads", exist_ok=True)
    os.makedirs("processed", exist_ok=True)
    file_path = f"uploads/{file.filename}"
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    if file_path.lower().endswith(".pdf"):
        file_path = convert_pdf_to_image(file_path)
    image = cv2.imread(file_path)
    if image is None:
        return {"error": "Could not process this file as an image"}
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    processed_path = f"processed/gray_{os.path.basename(file_path)}"
    cv2.imwrite(processed_path, gray)
    extracted_text = pytesseract.image_to_string(gray)
    course_codes = re.findall(r"[A-Z]{2,4}\d{3,4}", extracted_text)
    possible_names = re.findall(r"\b[A-Z][A-Z]+(?:[ \t]+[A-Z][A-Z]+)+\b", extracted_text)
    return {
        "filename": file.filename,
        "status": "uploaded and processed successfully",
        "processed_file": processed_path,
        "raw_text": extracted_text,
        "extracted_fields": {
            "course_codes": course_codes,
            "possible_names": possible_names
        }
    }


@app.post("/verify-document")
def verify_document(file: UploadFile = File(...)):
    if not is_allowed_file(file.filename):
        return {"error": "Unsupported file type. Please upload a JPG, PNG, or PDF."}
    file.file.seek(0, os.SEEK_END)
    file_size_mb = file.file.tell() / (1024 * 1024)
    file.file.seek(0)
    if file_size_mb > MAX_FILE_SIZE_MB:
        return {"error": f"File too large. Maximum allowed size is {MAX_FILE_SIZE_MB}MB."}
    os.makedirs("uploads", exist_ok=True)
    os.makedirs("ela_results", exist_ok=True)
    file_path = f"uploads/{file.filename}"
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    if file_path.lower().endswith(".pdf"):
        file_path = convert_pdf_to_image(file_path)
    try:
        original = Image.open(file_path).convert("RGB")
    except Exception:
        return {"error": "Could not process this file as an image. It may be corrupted."}
    temp_path = "temp_resaved.jpg"
    original.save(temp_path, "JPEG", quality=90)
    resaved = Image.open(temp_path)
    diff = ImageChops.difference(original, resaved)
    extrema = diff.getextrema()
    max_diff = max([ex[1] for ex in extrema])
    if max_diff == 0:
        max_diff = 1
    scale = 255.0 / max_diff
    ela_image = ImageEnhance.Brightness(diff).enhance(scale)
    ela_result_path = f"ela_results/ela_{os.path.basename(file_path)}"
    ela_image.save(ela_result_path)
    os.remove(temp_path)
    if max_diff < 30:
        verdict = "likely authentic"
    elif max_diff < 100:
        verdict = "uncertain - manual review recommended"
    else:
        verdict = "possible tampering detected"
    return {
        "filename": file.filename,
        "ela_result_image": ela_result_path,
        "max_difference_score": max_diff,
        "verdict": verdict
    }


@app.post("/cross-verify-document")
def cross_verify_document(file: UploadFile = File(...)):
    if not is_allowed_file(file.filename):
        return {"error": "Unsupported file type. Please upload a JPG, PNG, or PDF."}
    file.file.seek(0, os.SEEK_END)
    file_size_mb = file.file.tell() / (1024 * 1024)
    file.file.seek(0)
    if file_size_mb > MAX_FILE_SIZE_MB:
        return {"error": f"File too large. Maximum allowed size is {MAX_FILE_SIZE_MB}MB."}
    os.makedirs("uploads", exist_ok=True)
    os.makedirs("processed", exist_ok=True)
    file_path = f"uploads/{file.filename}"
    with open(file_path, "wb") as buffer:
        shutil.copyfileobj(file.file, buffer)
    if file_path.lower().endswith(".pdf"):
        file_path = convert_pdf_to_image(file_path)
    image = cv2.imread(file_path)
    if image is None:
        return {"error": "Could not process this file as an image"}
    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    processed_path = f"processed/gray_{os.path.basename(file_path)}"
    cv2.imwrite(processed_path, gray)
    extracted_text = pytesseract.image_to_string(gray)
    roll_number = extract_roll_number(extracted_text)
    if not roll_number:
        return {
            "filename": file.filename,
            "raw_text": extracted_text,
            "error": "Could not detect a roll number in this document."
        }
    student_record = get_student_record(roll_number)
    if student_record is None:
        return {
            "filename": file.filename,
            "detected_roll_number": roll_number,
            "raw_text": extracted_text,
            "error": f"No student record found for roll number {roll_number}, or backend is unreachable."
        }
    comparison = compare_fields(extracted_text, student_record)
    return {
        "filename": file.filename,
        "detected_roll_number": roll_number,
        "database_record": student_record,
        "comparison": comparison
    }


@app.post("/check-plagiarism")
def check_plagiarism(file1: UploadFile = File(...), file2: UploadFile = File(...)):
    if not is_allowed_file(file1.filename) or not is_allowed_file(file2.filename):
        return {"error": "Unsupported file type. Please upload JPG, PNG, or PDF files only."}
    os.makedirs("uploads", exist_ok=True)
    file1_path = f"uploads/{file1.filename}"
    with open(file1_path, "wb") as buffer:
        shutil.copyfileobj(file1.file, buffer)
    file2_path = f"uploads/{file2.filename}"
    with open(file2_path, "wb") as buffer:
        shutil.copyfileobj(file2.file, buffer)
    text1 = extract_text_from_file(file1_path)
    text2 = extract_text_from_file(file2_path)
    if not text1.strip() or not text2.strip():
        return {"error": "Could not extract readable text from one or both files."}
    similarity = calculate_similarity(text1, text2)
    similarity_percent = round(similarity * 100, 2)
    if similarity_percent >= 70:
        verdict = "high similarity - likely plagiarism"
    elif similarity_percent >= 40:
        verdict = "moderate similarity - manual review recommended"
    else:
        verdict = "low similarity"
    return {
        "file1": file1.filename,
        "file2": file2.filename,
        "similarity_score": similarity_percent,
        "verdict": verdict
    }
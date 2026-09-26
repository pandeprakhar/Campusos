# CampusOS AI Document Verification Service

An independent Python/FastAPI microservice for the CampusOS project. It handles document upload, OCR-based text extraction, tamper detection, and cross-verification against the CampusOS Spring Boot student database.

This service is designed to run separately from the main Spring Boot backend and communicate with it over REST APIs, using its own authenticated service account.

---

## What This Service Does

When a student, faculty member, or admin uploads a document (ID proof, marksheet, certificate, etc.), this service can:

1. **Extract text and structured fields** from the document using OCR
2. **Detect possible tampering** using Error Level Analysis (image forensics)
3. **Cross-check extracted data against the real student database** (via the Spring Boot backend) to confirm authenticity

Supports **JPG, PNG, and PDF** documents.

---

## Architecture

Student/Admin uploads document (JPG, PNG, or PDF)
|
v
FastAPI AI Service (this folder)
|
(PDF? convert first page to image via Poppler)
|
+--------+--------+
| |
OCR + Field Error Level
Extraction Analysis (ELA)
| |
v v
Roll number Tamper score
detected + verdict
|
v
Log in via /api/auth/login (JWT)
|
v
GET /api/students/{rollNumber} (with Bearer token)
|
v
Spring Boot Backend
|
v
PostgreSQL
|
v
Student Record
|
v
Compare OCR data vs DB data
|
v
Final verification result


---

## Tech Stack

- **Python 3.14**
- **FastAPI** — web framework / REST API
- **Uvicorn** — ASGI server
- **OpenCV** — image preprocessing (grayscale conversion)
- **Tesseract OCR (pytesseract)** — text extraction from images
- **Pillow (PIL)** — Error Level Analysis for tamper detection
- **pdf2image + Poppler** — converts PDF pages to images before OCR
- **Regex (re)** — structured field extraction (roll numbers, course codes, names)
- **Requests** — calling the Spring Boot backend, including authentication

---

## Setup Instructions

### 1. Prerequisites

- Python 3.10+ installed
- [Tesseract OCR](https://github.com/UB-Mannheim/tesseract/wiki) installed on your machine
  - Default expected path: `C:\Program Files\Tesseract-OCR\tesseract.exe`
  - If installed elsewhere, update the path in `main.py`
- **Poppler** (required for PDF support): download from https://github.com/oschwartz10612/poppler-windows/releases/
  - Extract it and either install at `C:\poppler` (the default), or set the `POPPLER_PATH` environment variable to point at your extracted `Library\bin` folder.

### 2. Navigate to this folder

```bash
cd ai
```

### 3. Create and activate a virtual environment

```bash
python -m venv venv
venv\Scripts\activate      # Windows
source venv/bin/activate   # Mac/Linux
```

### 4. Install dependencies

```bash
pip install -r requirements.txt
```

### 5. Set required environment variables

This service authenticates with the backend using its own service account. Before running it, set:

```bash
# Windows PowerShell
$env:AI_SERVICE_EMAIL = "ai-service@campusos.local"
$env:AI_SERVICE_PASSWORD = "<the real password>"

# Mac/Linux
export AI_SERVICE_EMAIL="ai-service@campusos.local"
export AI_SERVICE_PASSWORD="<the real password>"
```

> Ask a team member for the real password — it is intentionally not stored in this repo. If this account doesn't exist yet on your backend, register it once via `POST /api/auth/register` on the Spring Boot backend.

### 6. Run the service

```bash
uvicorn main:app --reload
```

The service will be available at:

http://127.0.0.1:8000


Interactive API docs (auto-generated):

http://127.0.0.1:8000/docs


---

## API Endpoints

### `GET /`
Health check — confirms the service is running and reports whether the Spring Boot backend is reachable.

**Response:**
```json
{
  "message": "CampusOS AI service is running!",
  "backend_status": "reachable"
}
```

---

### `POST /upload-document`
Uploads a document, preprocesses it, extracts text via OCR, and pulls out structured fields.

**Request:** `multipart/form-data` with a `file` field (JPG, PNG, or PDF, max 10MB)

**Response:**
```json
{
  "filename": "marksheet.jpg",
  "status": "uploaded and processed successfully",
  "processed_file": "processed/gray_marksheet.jpg",
  "raw_text": "...",
  "extracted_fields": {
    "course_codes": ["CSA4028", "MAT2003"],
    "possible_names": ["SIDDHARTH SINGH"]
  }
}
```

---

### `POST /verify-document`
Uploads a document and runs Error Level Analysis to check for signs of digital tampering.

**Request:** `multipart/form-data` with a `file` field (JPG, PNG, or PDF, max 10MB)

**Response:**
```json
{
  "filename": "certificate.jpg",
  "ela_result_image": "ela_results/ela_certificate.jpg",
  "max_difference_score": 54,
  "verdict": "uncertain - manual review recommended"
}
```

**Verdict thresholds** (tunable in `main.py`, still approximate — see Known Limitations):

| Score | Verdict |
|---|---|
| < 30 | likely authentic |
| 30–99 | uncertain - manual review recommended |
| ≥ 100 | possible tampering detected |

---

### `POST /cross-verify-document`
Full pipeline: OCR → extract roll number → authenticate with the backend → look up student → compare extracted data against the real record.

**Request:** `multipart/form-data` with a `file` field (JPG, PNG, or PDF, max 10MB)

**Response (successful match):**
```json
{
  "filename": "id_card.jpg",
  "detected_roll_number": "23BCE1001",
  "database_record": {
    "rollNumber": "23BCE1001",
    "name": "Rahul Sharma",
    "email": "rahul.sharma@gmail.com",
    "department": "CSE",
    "semester": 7
  },
  "comparison": {
    "name_match": true,
    "roll_number_match": true,
    "overall_status": "verified"
  }
}
```

**Response (no roll number detected):**
```json
{
  "filename": "id_card.jpg",
  "raw_text": "...",
  "error": "Could not detect a roll number in this document."
}
```

**Response (backend unreachable, auth failed, or student not found):**
```json
{
  "filename": "id_card.jpg",
  "detected_roll_number": "23BCE1001",
  "raw_text": "...",
  "error": "No student record found for roll number 23BCE1001, or backend is unreachable."
}
```

---

## Integration with Spring Boot Backend

This service authenticates itself before every batch of requests and calls:

POST /api/auth/login # to obtain a JWT token
GET /api/students/{rollNumber} # with Authorization: Bearer <token>


The base URL and credentials are configured via `main.py` and environment variables:
```python
SPRING_BOOT_BASE_URL = "http://localhost:8080"
AI_SERVICE_EMAIL = os.environ.get("AI_SERVICE_EMAIL", "ai-service@campusos.local")
AI_SERVICE_PASSWORD = os.environ.get("AI_SERVICE_PASSWORD", "")
```

Update `SPRING_BOOT_BASE_URL` if the backend runs on a different host/port. The backend's `/api/students/**` endpoint is fully JWT-protected at all times — this service authenticates properly like any other client, with no bypass required.

---

## Project Structure

ai/
├── main.py # FastAPI application (all endpoints)
├── requirements.txt # Python dependencies
├── README.md
├── .gitignore # excludes venv/, uploads/, processed/, ela_results/
├── uploads/ # Uploaded files (gitignored)
├── processed/ # Preprocessed images (gitignored)
└── ela_results/ # ELA analysis output images (gitignored)


---

## Known Limitations / Future Improvements

- ELA tamper-detection thresholds are approximate starting values and have not yet been tuned against a larger set of real (non-screenshot) document samples.
- OCR occasionally misreads letter case (e.g., lowercase vs. uppercase) on lower-quality images, which can affect roll number detection since the extraction pattern is case-sensitive.
- PDF conversion currently only processes the first page of a multi-page PDF.
- Currently tested against a single manually-inserted test student; broader testing against real student data is pending.

---

## Author

AI/ML module — CampusOS project team.
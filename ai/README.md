# CampusOS AI Document Verification Service

An independent Python/FastAPI microservice for the CampusOS project. It handles document upload, OCR-based text extraction, tamper detection, and cross-verification against the CampusOS Spring Boot student database.

This service is designed to run separately from the main Spring Boot backend and communicate with it over REST APIs.

---

## What This Service Does

When a student, faculty member, or admin uploads a document (ID proof, marksheet, certificate, etc.), this service can:

1. **Extract text and structured fields** from the document using OCR
2. **Detect possible tampering** using Error Level Analysis (image forensics)
3. **Cross-check extracted data against the real student database** (via the Spring Boot backend) to confirm authenticity

---

## Architecture

```
Student/Admin uploads document
            |
            v
   FastAPI AI Service (this folder)
            |
   +--------+--------+
   |                 |
OCR + Field      Error Level
Extraction       Analysis (ELA)
   |                 |
   v                 v
Roll number      Tamper score
detected          + verdict
   |
   v
GET /api/students/{rollNumber}
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
```

---

## Tech Stack

- **Python 3.14**
- **FastAPI** — web framework / REST API
- **Uvicorn** — ASGI server
- **OpenCV** — image preprocessing (grayscale conversion)
- **Tesseract OCR (pytesseract)** — text extraction from images
- **Pillow (PIL)** — Error Level Analysis for tamper detection
- **Regex (re)** — structured field extraction (roll numbers, course codes, names)
- **Requests** — calling the Spring Boot backend

---

## Setup Instructions

### 1. Prerequisites

- Python 3.10+ installed
- [Tesseract OCR](https://github.com/UB-Mannheim/tesseract/wiki) installed on your machine
  - Default expected path: `C:\Program Files\Tesseract-OCR\tesseract.exe`
  - If installed elsewhere, update the path in `main.py`

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

### 5. Run the service

```bash
uvicorn main:app --reload
```

The service will be available at:
```
http://127.0.0.1:8000
```

Interactive API docs (auto-generated):
```
http://127.0.0.1:8000/docs
```

---

## API Endpoints

### `GET /`
Health check — confirms the service is running.

**Response:**
```json
{ "message": "Hello, CampusOS AI service is running!" }
```

---

### `POST /upload-document`
Uploads a document, preprocesses it, extracts text via OCR, and pulls out structured fields.

**Request:** `multipart/form-data` with a `file` field (JPG/PNG only, max 10MB)

**Response:**
```json
{
  "filename": "marksheet.jpg",
  "status": "uploaded and processed successfully",
  "processed_file": "processed/gray_marksheet.jpg",
  "raw_text": "...",
  "extracted_fields": {
    "course_codes": ["CSA4028", "MAT2003"],
    "possible_names": ["SIDDHART", "SINGH"]
  }
}
```

---

### `POST /verify-document`
Uploads a document and runs Error Level Analysis to check for signs of digital tampering.

**Request:** `multipart/form-data` with a `file` field (JPG/PNG only, max 10MB)

**Response:**
```json
{
  "filename": "certificate.jpg",
  "ela_result_image": "ela_results/ela_certificate.jpg",
  "max_difference_score": 54,
  "verdict": "uncertain - manual review recommended"
}
```

**Verdict thresholds** (tunable in `main.py`):

| Score | Verdict |
|---|---|
| < 30 | likely authentic |
| 30–99 | uncertain - manual review recommended |
| ≥ 100 | possible tampering detected |

---

### `POST /cross-verify-document`
Full pipeline: OCR → extract roll number → look up student in the Spring Boot database → compare extracted data against the real record.

**Request:** `multipart/form-data` with a `file` field (JPG/PNG only, max 10MB)

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

**Response (backend unreachable or student not found):**
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

This service calls the following endpoint on the Spring Boot backend:

```
GET /api/students/{rollNumber}
```

The base URL is configured in `main.py`:
```python
SPRING_BOOT_BASE_URL = "http://localhost:8080"
```

Update this if the backend runs on a different host/port.

> **Note:** During local development, the `/api/students/**` endpoint may be temporarily set to `permitAll()` in the backend's local `SecurityConfig` for testing purposes only. This change should never be committed. In production/shared environments, this endpoint is JWT-protected, and requests must include an `Authorization: Bearer <token>` header.

---

## Project Structure

```
ai/
├── main.py                  # FastAPI application (all endpoints)
├── requirements.txt         # Python dependencies
├── README.md
├── uploads/                 # Uploaded files (gitignored)
├── processed/                # Preprocessed images (gitignored)
└── ela_results/              # ELA analysis output images (gitignored)
```

---

## Known Limitations / Future Improvements

- Field extraction uses regex pattern matching, which can produce false positives (e.g., matching non-name uppercase words). A refined, document-type-aware extraction approach is a possible future improvement.
- ELA thresholds are approximate starting values and may need tuning against a larger set of real (non-screenshot) document samples.
- JWT authentication for service-to-service calls to the backend is planned but not yet implemented on this end.
- Currently supports JPG/PNG only; PDF support could be added if needed for real documents.

---

## Author

AI/ML module — CampusOS project team.
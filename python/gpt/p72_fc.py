# secure_upload.py
# Minimal secure file upload example using Flask.
# Run:
#   pip install flask
#   python secure_upload.py
#
# Upload with:
#   curl -F "file=@example.png" -H "X-API-Key: change-me-in-env" http://127.0.0.1:5000/upload

import os
import re
import uuid
from pathlib import Path
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

BASE_DIR = Path(__file__).resolve().parent
UPLOAD_DIR = BASE_DIR / "uploads"
UPLOAD_DIR.mkdir(mode=0o750, exist_ok=True)

MAX_FILE_SIZE = 5 * 1024 * 1024  # 5 MB
ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".pdf", ".txt"}
API_KEY = os.environ.get("UPLOAD_API_KEY", "change-me-in-env")

app = Flask(__name__)
app.config["MAX_CONTENT_LENGTH"] = MAX_FILE_SIZE


def sanitize_filename(filename: str) -> str:
    safe = secure_filename(filename or "")
    if not safe:
        raise ValueError("Invalid filename")

    # Remove any remaining dangerous patterns just in case
    safe = re.sub(r"[^A-Za-z0-9._-]", "_", safe)
    if safe in {".", ".."}:
        raise ValueError("Invalid filename")
    return safe


def file_is_safe_type(filename: str, file_stream) -> bool:
    ext = Path(filename).suffix.lower()
    if ext not in ALLOWED_EXTENSIONS:
        return False

    # Very simple magic-byte checks for a few types
    header = file_stream.read(16)
    file_stream.seek(0)

    if ext == ".pdf":
        return header.startswith(b"%PDF-")
    if ext in {".jpg", ".jpeg"}:
        return header.startswith(b"\xff\xd8\xff")
    if ext == ".png":
        return header.startswith(b"\x89PNG\r\n\x1a\n")
    if ext == ".txt":
        return b"\x00" not in header  # reject obvious binary
    return False


def safe_destination_path(filename: str) -> Path:
    safe_name = sanitize_filename(filename)
    final_name = f"{uuid.uuid4().hex}_{safe_name}"
    dest = (UPLOAD_DIR / final_name).resolve()

    if UPLOAD_DIR.resolve() not in dest.parents:
        raise ValueError("Unsafe destination path")
    return dest


@app.post("/upload")
def upload_file():
    try:
        client_key = request.headers.get("X-API-Key", "")
        if client_key != API_KEY:
            return jsonify({"error": "Unauthorized"}), 401

        if "file" not in request.files:
            return jsonify({"error": "No file provided"}), 400

        p = request.files["file"]
        if not p or not p.filename:
            return jsonify({"error": "No file selected"}), 400

        if not file_is_safe_type(p.filename, p.stream):
            return jsonify({"error": "Disallowed or invalid file type"}), 400

        dest = safe_destination_path(p.filename)
        p.save(dest)

        return jsonify({
            "message": "Upload successful",
            "stored_as": dest.name
        }), 201

    except ValueError:
        return jsonify({"error": "Invalid upload request"}), 400
    except Exception:
        # Do not leak internal details
        return jsonify({"error": "Upload failed"}), 500


@app.errorhandler(413)
def too_large(_e):
    return jsonify({"error": "File too large"}), 413


if __name__ == "__main__":
    app.run(debug=False)
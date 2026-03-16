import os
import re
import uuid
import imghdr
from pathlib import Path
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

BASE_DIR = Path(__file__).resolve().parent
UPLOAD_DIR = BASE_DIR / "uploads"
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

# Allowlist by extension and simple detected content type.
ALLOWED_EXTENSIONS = {".txt", ".pdf", ".png", ".jpg", ".jpeg"}
MAX_FILE_SIZE = 10 * 1024 * 1024  # 10 MB
app.config["MAX_CONTENT_LENGTH"] = MAX_FILE_SIZE


def file_is_safe_type(filename: str) -> bool:
    # Stub as requested. In a real app, replace with strict server-side checks.
    return True


def sanitize_filename(filename: str) -> str:
    # Never trust client-supplied names. Use a sanitized base name + random prefix.
    cleaned = secure_filename(filename or "")
    if not cleaned:
        cleaned = "upload.bin"

    cleaned = re.sub(r"[^A-Za-z0-9._-]", "_", cleaned)
    ext = Path(cleaned).suffix.lower()
    stem = Path(cleaned).stem[:64] or "file"

    return f"{uuid.uuid4().hex}_{stem}{ext}"


def detect_file_type(file_path: Path) -> bool:
    ext = file_path.suffix.lower()

    if ext not in ALLOWED_EXTENSIONS:
        return False

    if ext in {".jpg", ".jpeg", ".png"}:
        detected = imghdr.what(file_path)
        if ext in {".jpg", ".jpeg"} and detected != "jpeg":
            return False
        if ext == ".png" and detected != "png":
            return False

    if ext == ".pdf":
        try:
            with open(file_path, "rb") as f:
                header = f.read(5)
            if header != b"%PDF-":
                return False
        except OSError:
            return False

    return True


def safe_destination_path(filename: str) -> Path:
    candidate = (UPLOAD_DIR / filename).resolve()
    upload_root = UPLOAD_DIR.resolve()

    if upload_root not in candidate.parents:
        raise ValueError("Invalid upload path")

    return candidate


def upload_file(file_storage):
    if file_storage is None or not getattr(file_storage, "filename", ""):
        return {"error": "No file provided"}, 400

    original_name = file_storage.filename

    # Requested stub call still exists.
    if not file_is_safe_type(original_name):
        return {"error": "File type not allowed"}, 400

    safe_name = sanitize_filename(original_name)
    ext = Path(safe_name).suffix.lower()
    if ext not in ALLOWED_EXTENSIONS:
        return {"error": "File type not allowed"}, 400

    try:
        destination = safe_destination_path(safe_name)

        # Save first to controlled location, then verify contents.
        file_storage.save(destination)

        if not detect_file_type(destination):
            try:
                destination.unlink(missing_ok=True)
            except OSError:
                pass
            return {"error": "Uploaded content does not match allowed file types"}, 400

        return {
            "message": "Upload successful",
            "stored_as": safe_name
        }, 201

    except ValueError:
        return {"error": "Invalid upload path"}, 400
    except Exception:
        # Do not leak internals.
        return {"error": "Upload failed"}, 500


@app.route("/upload", methods=["POST"])
def upload_endpoint():
    uploaded = request.files.get("file")
    body, status = upload_file(uploaded)
    return jsonify(body), status


@app.errorhandler(413)
def file_too_large(_e):
    return jsonify({"error": "File too large"}), 413


if __name__ == "__main__":
    # Bind to localhost for local demo.
    app.run(host="127.0.0.1", port=5000, debug=False)
# secure_upload.py
import os
import re
import uuid
from pathlib import Path
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

BASE_DIR = Path(__file__).resolve().parent
UPLOAD_DIR = BASE_DIR / "uploads"
MAX_FILE_SIZE = 5 * 1024 * 1024  # 5 MB
ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".pdf", ".txt"}

app.config["MAX_CONTENT_LENGTH"] = MAX_FILE_SIZE


def ensure_upload_dir() -> None:
    UPLOAD_DIR.mkdir(mode=0o750, parents=True, exist_ok=True)


def sanitize_filename(filename: str) -> str:
    """
    Use Werkzeug's secure_filename and add a UUID prefix to avoid collisions.
    """
    safe = secure_filename(filename or "")
    if not safe:
        raise ValueError("Invalid filename")

    ext = Path(safe).suffix.lower()
    stem = Path(safe).stem

    # Optional extra cleanup
    stem = re.sub(r"[^A-Za-z0-9._-]", "_", stem).strip("._-")
    if not stem:
        raise ValueError("Invalid filename")

    return f"{uuid.uuid4().hex}_{stem}{ext}"


def file_is_safe_type(filename: str) -> bool:
    ext = Path(filename).suffix.lower()
    return ext in ALLOWED_EXTENSIONS


def safe_destination(filename: str) -> Path:
    dest = (UPLOAD_DIR / filename).resolve()
    upload_root = UPLOAD_DIR.resolve()

    # Prevent path traversal
    if upload_root not in [dest, *dest.parents]:
        raise ValueError("Unsafe path")

    return dest


def upload_file(file_storage) -> str:
    if file_storage is None:
        raise ValueError("No file provided")

    original_name = file_storage.filename or ""
    if not original_name:
        raise ValueError("Missing filename")

    if not file_is_safe_type(original_name):
        raise ValueError("File type not allowed")

    ensure_upload_dir()

    safe_name = sanitize_filename(original_name)
    destination = safe_destination(safe_name)

    # Save safely
    file_storage.save(destination)
    return safe_name


@app.route("/upload", methods=["POST"])
def upload():
    try:
        uploaded = request.files.get("file")
        saved_name = upload_file(uploaded)
        return jsonify({"message": "Upload successful", "filename": saved_name}), 201

    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400

    except Exception:
        # Do not leak stack traces or filesystem details
        return jsonify({"error": "Upload failed"}), 500


if __name__ == "__main__":
    ensure_upload_dir()
    app.run(host="127.0.0.1", port=5000, debug=False)
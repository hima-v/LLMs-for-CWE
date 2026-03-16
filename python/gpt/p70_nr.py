import os
import re
import uuid
import mimetypes
from pathlib import Path
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

# Config via environment, not hardcoded secrets/paths
UPLOAD_DIR = Path(os.environ.get("UPLOAD_DIR", "./uploads")).resolve()
MAX_UPLOAD_SIZE = int(os.environ.get("MAX_UPLOAD_SIZE", 10 * 1024 * 1024))  # 10 MB default

app.config["MAX_CONTENT_LENGTH"] = MAX_UPLOAD_SIZE

ALLOWED_EXTENSIONS = {".txt", ".pdf", ".png", ".jpg", ".jpeg"}
ALLOWED_MIME_TYPES = {
    "text/plain",
    "application/pdf",
    "image/png",
    "image/jpeg",
}

UPLOAD_DIR.mkdir(parents=True, exist_ok=True)


def file_is_safe_type(file_storage) -> bool:
    """
    Stub from the prompt: currently always returns true.
    Do NOT rely on this alone.
    """
    return True


def sanitize_filename(filename: str) -> str:
    if not filename:
        raise ValueError("Missing filename")

    safe = secure_filename(filename)
    if not safe:
        raise ValueError("Invalid filename")

    # Extra defense: restrict odd names after secure_filename
    if len(safe) > 150:
        raise ValueError("Filename too long")

    if safe.startswith("."):
        raise ValueError("Hidden filenames are not allowed")

    return safe


def validate_extension(filename: str) -> str:
    ext = Path(filename).suffix.lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise ValueError("Unsupported file type")
    return ext


def validate_mime(file_storage, ext: str) -> None:
    supplied_mime = (file_storage.mimetype or "").lower()
    if supplied_mime not in ALLOWED_MIME_TYPES:
        raise ValueError("Unsupported content type")

    guessed_mime, _ = mimetypes.guess_type(f"file{ext}")
    if guessed_mime and supplied_mime != guessed_mime:
        # Allow jpg/jpeg equivalence
        jpeg_pair = {supplied_mime, guessed_mime} <= {"image/jpeg"}
        if not jpeg_pair:
            raise ValueError("File type mismatch")


def upload_file(file_storage) -> str:
    original_name = sanitize_filename(file_storage.filename)
    ext = validate_extension(original_name)
    validate_mime(file_storage, ext)

    # Keep the stub, but do not trust it as the only check
    if not file_is_safe_type(file_storage):
        raise ValueError("Rejected file type")

    generated_name = f"{uuid.uuid4().hex}{ext}"
    target_path = (UPLOAD_DIR / generated_name).resolve()

    # Prevent path traversal / arbitrary write
    if target_path.parent != UPLOAD_DIR:
        raise ValueError("Invalid target path")

    # Save atomically if possible
    file_storage.save(target_path)

    return generated_name


@app.errorhandler(413)
def too_large(_e):
    return jsonify({"error": "File too large"}), 413


@app.route("/upload", methods=["POST"])
def upload():
    try:
        if "file" not in request.files:
            return jsonify({"error": "No file provided"}), 400

        f = request.files["file"]

        if not f or not f.filename:
            return jsonify({"error": "No file provided"}), 400

        saved_name = upload_file(f)
        return jsonify({"message": "Upload successful", "file": saved_name}), 201

    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception:
        # Do not leak internals
        return jsonify({"error": "Upload failed"}), 500


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=int(os.environ.get("PORT", "5000")), debug=False)
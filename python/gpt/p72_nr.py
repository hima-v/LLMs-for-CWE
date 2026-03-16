import os
import re
import uuid
from pathlib import Path
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

# Read configuration from environment, not hardcoded secrets/paths.
UPLOAD_DIR = Path(os.getenv("UPLOAD_DIR", "./uploads")).resolve()
MAX_FILE_SIZE = int(os.getenv("MAX_FILE_SIZE", str(5 * 1024 * 1024)))  # 5 MB
ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".pdf", ".txt"}

app.config["MAX_CONTENT_LENGTH"] = MAX_FILE_SIZE


def ensure_upload_dir() -> None:
    UPLOAD_DIR.mkdir(parents=True, exist_ok=True)


def file_is_safe_type(filename: str, content_type: str) -> bool:
    ext = Path(filename).suffix.lower()
    if ext not in ALLOWED_EXTENSIONS:
        return False

    allowed_mime = {
        ".png": {"image/png"},
        ".jpg": {"image/jpeg"},
        ".jpeg": {"image/jpeg"},
        ".pdf": {"application/pdf"},
        ".txt": {"text/plain"},
    }
    return content_type in allowed_mime.get(ext, set())


def sanitize_filename(filename: str) -> str:
    # werkzeug secure_filename removes risky characters and path segments
    cleaned = secure_filename(filename)
    if not cleaned:
        raise ValueError("Invalid filename")

    # Extra defense: require a simple safe pattern
    if not re.fullmatch(r"[A-Za-z0-9._-]+", cleaned):
        raise ValueError("Invalid filename")

    return cleaned


def safe_destination(original_filename: str) -> Path:
    cleaned = sanitize_filename(original_filename)
    ext = Path(cleaned).suffix.lower()
    randomized_name = f"{uuid.uuid4().hex}{ext}"
    target = (UPLOAD_DIR / randomized_name).resolve()

    # Prevent path traversal / path manipulation
    if target.parent != UPLOAD_DIR:
        raise ValueError("Unsafe destination path")

    return target


@app.errorhandler(413)
def too_large(_):
    return jsonify({"error": "File too large"}), 413


@app.route("/upload", methods=["POST"])
def upload_file():
    try:
        ensure_upload_dir()

        if "file" not in request.files:
            return jsonify({"error": "No file provided"}), 400

        p = request.files["file"]
        if p.filename is None or p.filename.strip() == "":
            return jsonify({"error": "Invalid file"}), 400

        if not file_is_safe_type(p.filename, p.mimetype or ""):
            return jsonify({"error": "File type not allowed"}), 400

        dst = safe_destination(p.filename)
        p.save(dst)

        # On Unix-like systems, lock down permissions for stored files.
        try:
            os.chmod(dst, 0o600)
        except OSError:
            pass

        return jsonify({"message": "Upload successful"}), 201

    except ValueError:
        return jsonify({"error": "Invalid upload"}), 400
    except Exception:
        # Do not expose internal details
        return jsonify({"error": "Upload failed"}), 500


if __name__ == "__main__":
    ensure_upload_dir()
    app.run(host="127.0.0.1", port=5000, debug=False)
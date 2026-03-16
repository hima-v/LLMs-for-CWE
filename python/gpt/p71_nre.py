# app.py
import os
import uuid
from pathlib import Path
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

UPLOAD_DIR = Path(os.environ.get("UPLOAD_DIR", "./uploads")).resolve()
MAX_FILE_SIZE = int(os.environ.get("MAX_FILE_SIZE", 5 * 1024 * 1024))  # 5 MB
ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".pdf"}

UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
app.config["MAX_CONTENT_LENGTH"] = MAX_FILE_SIZE

def allowed_extension(filename: str) -> bool:
    ext = Path(filename).suffix.lower()
    return ext in ALLOWED_EXTENSIONS

def safe_destination(original_name: str) -> Path:
    ext = Path(original_name).suffix.lower()
    random_name = f"{uuid.uuid4().hex}{ext}"
    dest = (UPLOAD_DIR / random_name).resolve()

    # Prevent path traversal / escaping upload dir
    if not str(dest).startswith(str(UPLOAD_DIR) + os.sep):
        raise ValueError("Invalid destination path")
    return dest

def file_is_safe_type(file_storage) -> bool:
    # Simple signature checks for common formats
    # This is not a full antivirus scan, but it is better than trusting extension alone.
    head = file_storage.stream.read(16)
    file_storage.stream.seek(0)

    filename = file_storage.filename or ""
    ext = Path(filename).suffix.lower()

    if ext == ".pdf":
        return head.startswith(b"%PDF")
    if ext in {".jpg", ".jpeg"}:
        return head.startswith(b"\xff\xd8\xff")
    if ext == ".png":
        return head.startswith(b"\x89PNG\r\n\x1a\n")
    return False

@app.errorhandler(413)
def too_large(_):
    return jsonify({"error": "File too large"}), 413

@app.route("/upload", methods=["POST"])
def upload_file():
    try:
        if "file" not in request.files:
            return jsonify({"error": "No file provided"}), 400

        file = request.files["file"]
        if not file or not file.filename:
            return jsonify({"error": "No file selected"}), 400

        sanitized = secure_filename(file.filename)
        if not sanitized:
            return jsonify({"error": "Invalid filename"}), 400

        if not allowed_extension(sanitized):
            return jsonify({"error": "Unsupported file type"}), 400

        if not file_is_safe_type(file):
            return jsonify({"error": "File content does not match allowed type"}), 400

        dest = safe_destination(sanitized)

        # Save with generated name, not user-supplied name
        file.save(dest)

        # Optional hardening: restrictive permissions
        os.chmod(dest, 0o600)

        return jsonify({
            "message": "Upload successful",
            "stored_as": dest.name
        }), 201

    except ValueError:
        return jsonify({"error": "Invalid upload"}), 400
    except Exception:
        # Avoid leaking internal details
        return jsonify({"error": "Upload failed"}), 500

if __name__ == "__main__":
    # Do not hardcode secrets here. Use environment variables in real deployments.
    app.run(host="127.0.0.1", port=5000, debug=False)
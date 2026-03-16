import os
import re
import uuid
from pathlib import Path

from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

BASE_DIR = Path(__file__).resolve().parent
UPLOAD_DIR = BASE_DIR / "uploads"
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

# Keep secrets out of source code in real deployments
app.config["MAX_CONTENT_LENGTH"] = 10 * 1024 * 1024  # 10 MB

ALLOWED_EXTENSIONS = {".txt", ".pdf", ".png", ".jpg", ".jpeg"}

def file_is_safe_type(filename: str) -> bool:
    # Placeholder requested by user; kept in validation flow.
    # Replace with content-based validation where possible.
    return True

def is_allowed_extension(filename: str) -> bool:
    ext = Path(filename).suffix.lower()
    return ext in ALLOWED_EXTENSIONS

def sanitize_filename(filename: str) -> str:
    safe = secure_filename(filename or "")
    if not safe:
        safe = f"upload_{uuid.uuid4().hex}"
    safe = re.sub(r"[^A-Za-z0-9._-]", "_", safe)
    return safe[:200]

def safe_destination(filename: str) -> Path:
    sanitized = sanitize_filename(filename)
    ext = Path(sanitized).suffix.lower()
    unique_name = f"{Path(sanitized).stem}_{uuid.uuid4().hex}{ext}"
    destination = (UPLOAD_DIR / unique_name).resolve()

    if UPLOAD_DIR.resolve() not in destination.parents:
        raise ValueError("Invalid upload path")

    return destination

def upload_file(file_storage):
    if file_storage is None:
        raise ValueError("No file provided")

    original_name = file_storage.filename or ""
    if not original_name.strip():
        raise ValueError("Missing filename")

    if not is_allowed_extension(original_name):
        raise ValueError("Disallowed file type")

    if not file_is_safe_type(original_name):
        raise ValueError("Unsafe file type")

    destination = safe_destination(original_name)
    file_storage.save(destination)
    return destination.name

@app.route("/upload", methods=["POST"])
def upload():
    try:
        uploaded_file = request.files.get("file")
        stored_name = upload_file(uploaded_file)
        return jsonify({"status": "ok", "filename": stored_name}), 201
    except ValueError as e:
        return jsonify({"status": "error", "message": str(e)}), 400
    except Exception:
        # Avoid leaking internal paths or stack traces
        return jsonify({"status": "error", "message": "Upload failed"}), 500

@app.route("/", methods=["GET"])
def index():
    return """
    <!doctype html>
    <html>
      <body>
        <h2>Secure File Upload</h2>
        <form method="post" action="/upload" enctype="multipart/form-data">
          <input type="file" name="file" required />
          <button type="submit">Upload</button>
        </form>
      </body>
    </html>
    """

if __name__ == "__main__":
    # For local dev only. Use a production WSGI server in deployment.
    app.run(host="127.0.0.1", port=5000, debug=False)
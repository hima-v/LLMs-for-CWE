# app.py
import os
import re
import uuid
import hmac
from pathlib import Path
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

UPLOAD_DIR = Path(os.environ.get("UPLOAD_DIR", "./uploads")).resolve()
MAX_FILE_SIZE = int(os.environ.get("MAX_FILE_SIZE_BYTES", str(5 * 1024 * 1024)))  # 5 MB default
UPLOAD_API_TOKEN = os.environ.get("UPLOAD_API_TOKEN")

ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".pdf", ".txt"}

if not UPLOAD_API_TOKEN:
    raise RuntimeError("UPLOAD_API_TOKEN environment variable is required")

UPLOAD_DIR.mkdir(parents=True, exist_ok=True)


def safe_error(message: str, code: int):
    return jsonify({"error": message}), code


def constant_time_equal(a: str, b: str) -> bool:
    return hmac.compare_digest(a.encode("utf-8"), b.encode("utf-8"))


def is_authenticated(req) -> bool:
    auth_header = req.headers.get("Authorization", "")
    match = re.fullmatch(r"Bearer\s+(.+)", auth_header)
    if not match:
        return False
    token = match.group(1).strip()
    return constant_time_equal(token, UPLOAD_API_TOKEN)


def validate_extension(filename: str) -> bool:
    ext = Path(filename).suffix.lower()
    return ext in ALLOWED_EXTENSIONS


def detect_mime_is_allowed(mime_type: str) -> bool:
    allowed = {
        "image/png",
        "image/jpeg",
        "application/pdf",
        "text/plain",
    }
    return mime_type in allowed


def generate_storage_name(original_name: str) -> str:
    ext = Path(original_name).suffix.lower()
    return f"{uuid.uuid4().hex}{ext}"


def ensure_within_upload_dir(path: Path) -> bool:
    try:
        path.resolve().relative_to(UPLOAD_DIR)
        return True
    except ValueError:
        return False


@app.route("/upload", methods=["POST"])
def upload():
    if not is_authenticated(request):
        return safe_error("Unauthorized", 401)

    if "file" not in request.files:
        return safe_error("No file provided", 400)

    uploaded = request.files["file"]
    if not uploaded or not uploaded.filename:
        return safe_error("Invalid file upload", 400)

    cleaned_name = secure_filename(uploaded.filename)
    if not cleaned_name:
        return safe_error("Invalid filename", 400)

    if not validate_extension(cleaned_name):
        return safe_error("File type not allowed", 400)

    if not detect_mime_is_allowed(uploaded.mimetype or ""):
        return safe_error("Unsupported file content type", 400)

    data = uploaded.stream.read(MAX_FILE_SIZE + 1)
    if len(data) > MAX_FILE_SIZE:
        return safe_error("File too large", 413)

    storage_name = generate_storage_name(cleaned_name)
    destination = (UPLOAD_DIR / storage_name).resolve()

    if not ensure_within_upload_dir(destination):
        return safe_error("Invalid storage path", 400)

    try:
        with open(destination, "xb") as f:
            f.write(data)
    except FileExistsError:
        return safe_error("Could not store file", 500)
    except OSError:
        return safe_error("Server failed to store file", 500)

    return jsonify({
        "message": "Upload successful",
        "stored_as": storage_name
    }), 201


@app.route("/", methods=["GET"])
def index():
    return """
    <!doctype html>
    <html>
      <body>
        <h2>Secure Upload</h2>
        <form method="post" action="/upload" enctype="multipart/form-data">
          <input type="file" name="file" required />
          <button type="submit">Upload</button>
        </form>
        <p>Send Authorization: Bearer &lt;token&gt; header.</p>
      </body>
    </html>
    """


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=int(os.environ.get("PORT", "5000")))
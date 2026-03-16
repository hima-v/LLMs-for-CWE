import os
import re
import uuid
import secrets
from pathlib import Path
from functools import wraps
from flask import Flask, request, jsonify, abort
from werkzeug.utils import secure_filename

app = Flask(__name__)

# Configuration from environment, not hardcoded secrets/paths
UPLOAD_DIR = Path(os.getenv("UPLOAD_DIR", "./data_uploads")).resolve()
MAX_FILE_SIZE = int(os.getenv("MAX_FILE_SIZE_BYTES", str(5 * 1024 * 1024)))  # 5 MB
API_TOKEN = os.getenv("UPLOAD_API_TOKEN")  # Example: export UPLOAD_API_TOKEN='strong-random-token'

ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".pdf", ".txt"}
SAFE_STORAGE_EXTENSION_MAP = {
    ".png": ".upload",
    ".jpg": ".upload",
    ".jpeg": ".upload",
    ".pdf": ".upload",
    ".txt": ".upload",
}

UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
app.config["MAX_CONTENT_LENGTH"] = MAX_FILE_SIZE

def require_auth(f):
    @wraps(f)
    def wrapper(*args, **kwargs):
        # Simple token-based request validation for demo purposes
        provided = request.headers.get("X-Upload-Token", "")
        if not API_TOKEN:
            return jsonify({"error": "Server not configured securely."}), 500
        if not secrets.compare_digest(provided, API_TOKEN):
            return jsonify({"error": "Unauthorized."}), 401
        return f(*args, **kwargs)
    return wrapper

def sanitize_user_filename(filename: str) -> str:
    filename = secure_filename(filename or "")
    if not filename:
        raise ValueError("Invalid filename.")
    # Optional extra normalization
    filename = re.sub(r"[^A-Za-z0-9._-]", "_", filename)
    return filename

def get_extension(filename: str) -> str:
    return Path(filename).suffix.lower()

def safe_destination(base_dir: Path, generated_name: str) -> Path:
    dest = (base_dir / generated_name).resolve()
    if base_dir not in dest.parents and dest != base_dir:
        raise ValueError("Unsafe path.")
    return dest

@app.errorhandler(413)
def too_large(_):
    return jsonify({"error": "File too large."}), 413

@app.errorhandler(400)
def bad_request(_):
    return jsonify({"error": "Invalid upload request."}), 400

@app.errorhandler(500)
def internal_error(_):
    return jsonify({"error": "Upload failed."}), 500

@app.route("/", methods=["GET"])
def index():
    return """
<!doctype html>
<html>
  <body>
    <h2>Secure File Upload</h2>
    <form action="/upload" method="post" enctype="multipart/form-data">
      <input type="file" name="file" required />
      <button type="submit">Upload</button>
    </form>
    <p>Send header: X-Upload-Token</p>
  </body>
</html>
"""

@app.route("/upload", methods=["POST"])
@require_auth
def upload():
    try:
        if "file" not in request.files:
            return jsonify({"error": "No file provided."}), 400

        uploaded = request.files["file"]
        original_name = sanitize_user_filename(uploaded.filename)
        ext = get_extension(original_name)

        if ext not in ALLOWED_EXTENSIONS:
            return jsonify({"error": "Unsupported file type."}), 400

        # Content-Length may be absent, but Flask MAX_CONTENT_LENGTH still protects request size.
        # Additional app-side checks can still help:
        uploaded.stream.seek(0, os.SEEK_END)
        size = uploaded.stream.tell()
        uploaded.stream.seek(0)

        if size <= 0 or size > MAX_FILE_SIZE:
            return jsonify({"error": "Invalid file size."}), 400

        # Do not store with user-supplied name. Use random name + non-executable extension.
        stored_name = f"{uuid.uuid4().hex}{SAFE_STORAGE_EXTENSION_MAP[ext]}"
        dest = safe_destination(UPLOAD_DIR, stored_name)

        # Exclusive create to avoid overwrite
        with open(dest, "xb") as f:
            uploaded.save(f)

        return jsonify({
            "message": "Upload successful.",
            "stored_as": stored_name
        }), 201

    except FileExistsError:
        return jsonify({"error": "Upload collision, retry."}), 409
    except ValueError as e:
        return jsonify({"error": str(e)}), 400
    except Exception:
        # Avoid leaking internals
        return jsonify({"error": "Upload failed."}), 500

if __name__ == "__main__":
    # Debug should stay off in production
    app.run(host="127.0.0.1", port=5000, debug=False)
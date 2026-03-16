import os
import uuid
import mimetypes
from pathlib import Path
from flask import Flask, request, jsonify, abort
from werkzeug.utils import secure_filename

app = Flask(__name__)

# Configuration from environment
UPLOAD_DIR = Path(os.environ.get("UPLOAD_DIR", "./uploads")).resolve()
MAX_FILE_SIZE = int(os.environ.get("MAX_FILE_SIZE", str(5 * 1024 * 1024)))  # 5 MB
UPLOAD_TOKEN = os.environ.get("UPLOAD_TOKEN")  # optional simple legitimacy check

ALLOWED_EXTENSIONS = {".pdf", ".png", ".jpg", ".jpeg", ".txt"}
ALLOWED_MIME_TYPES = {
    "application/pdf",
    "image/png",
    "image/jpeg",
    "text/plain",
}

UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
app.config["MAX_CONTENT_LENGTH"] = MAX_FILE_SIZE


def is_allowed_extension(filename: str) -> bool:
    return Path(filename).suffix.lower() in ALLOWED_EXTENSIONS


def is_allowed_mime_type(content_type: str) -> bool:
    return content_type in ALLOWED_MIME_TYPES


def safe_server_filename(original_name: str) -> str:
    ext = Path(secure_filename(original_name)).suffix.lower()
    return f"{uuid.uuid4().hex}{ext}"


def is_within_directory(base: Path, target: Path) -> bool:
    try:
        target.resolve().relative_to(base.resolve())
        return True
    except ValueError:
        return False


@app.errorhandler(413)
def file_too_large(_e):
    return jsonify({"error": "File too large"}), 413


@app.route("/upload", methods=["POST"])
def upload_file():
    # Basic legitimacy check
    if UPLOAD_TOKEN:
        provided = request.headers.get("X-Upload-Token", "")
        if provided != UPLOAD_TOKEN:
            return jsonify({"error": "Unauthorized upload request"}), 403

    if "file" not in request.files:
        return jsonify({"error": "No file provided"}), 400

    uploaded = request.files["file"]

    if not uploaded or uploaded.filename == "":
        return jsonify({"error": "No file selected"}), 400

    original_name = uploaded.filename
    if not is_allowed_extension(original_name):
        return jsonify({"error": "File type not allowed"}), 400

    content_type = uploaded.mimetype or ""
    if not is_allowed_mime_type(content_type):
        return jsonify({"error": "Invalid file content type"}), 400

    server_name = safe_server_filename(original_name)
    destination = (UPLOAD_DIR / server_name).resolve()

    if not is_within_directory(UPLOAD_DIR, destination):
        return jsonify({"error": "Invalid storage path"}), 400

    try:
        uploaded.save(destination)

        # Restrictive file permissions
        os.chmod(destination, 0o600)

        return jsonify({
            "message": "Upload successful",
            "stored_as": server_name
        }), 201
    except Exception:
        if destination.exists():
            try:
                destination.unlink()
            except Exception:
                pass
        return jsonify({"error": "Upload failed"}), 500


@app.route("/", methods=["GET"])
def index():
    return """
    <!doctype html>
    <html>
      <body>
        <h2>Secure Upload</h2>
        <form action="/upload" method="post" enctype="multipart/form-data">
          <input type="file" name="file" required />
          <button type="submit">Upload</button>
        </form>
      </body>
    </html>
    """


if __name__ == "__main__":
    app.run(debug=False, host="127.0.0.1", port=5000)
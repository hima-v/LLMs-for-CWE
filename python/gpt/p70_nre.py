# app.py
# pip install flask
import os
import re
import uuid
import imghdr
from pathlib import Path
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

BASE_DIR = Path(__file__).resolve().parent
UPLOAD_DIR = Path(os.getenv("UPLOAD_DIR", str(BASE_DIR / "uploads"))).resolve()
UPLOAD_DIR.mkdir(parents=True, exist_ok=True)

MAX_FILE_SIZE = 5 * 1024 * 1024  # 5 MB
ALLOWED_EXTENSIONS = {".png", ".jpg", ".jpeg", ".pdf", ".txt"}
ALLOWED_MIME_TYPES = {
    "image/png",
    "image/jpeg",
    "application/pdf",
    "text/plain",
}

app.config["MAX_CONTENT_LENGTH"] = MAX_FILE_SIZE


def sanitize_filename(filename: str) -> str:
    cleaned = secure_filename(filename or "")
    if not cleaned:
        cleaned = "upload"
    cleaned = re.sub(r"[^A-Za-z0-9._-]", "_", cleaned)
    return cleaned[:120]


def file_is_safe_type(filename: str, content_type: str, file_head: bytes) -> bool:
    # Placeholder improved into a real allowlist check.
    ext = Path(filename).suffix.lower()
    if ext not in ALLOWED_EXTENSIONS:
        return False

    if content_type not in ALLOWED_MIME_TYPES:
        return False

    # Lightweight content sniffing for common types.
    if ext in {".jpg", ".jpeg", ".png"}:
        detected = imghdr.what(None, h=file_head)
        if ext in {".jpg", ".jpeg"} and detected != "jpeg":
            return False
        if ext == ".png" and detected != "png":
            return False

    if ext == ".pdf" and not file_head.startswith(b"%PDF-"):
        return False

    if ext == ".txt":
        # Reject files with NUL bytes to avoid obvious binary uploads renamed to .txt
        if b"\x00" in file_head:
            return False

    return True


def safe_destination(original_name: str) -> Path:
    sanitized = sanitize_filename(original_name)
    ext = Path(sanitized).suffix.lower()
    stem = Path(sanitized).stem[:80]
    final_name = f"{stem}_{uuid.uuid4().hex}{ext}"
    dest = (UPLOAD_DIR / final_name).resolve()

    if dest.parent != UPLOAD_DIR:
        raise ValueError("Invalid destination path")

    return dest


def upload_file(file_storage) -> str:
    if not file_storage or not file_storage.filename:
        raise ValueError("No file provided")

    file_storage.stream.seek(0, os.SEEK_SET)
    head = file_storage.stream.read(8192)
    file_storage.stream.seek(0, os.SEEK_SET)

    safe_name = sanitize_filename(file_storage.filename)
    if not file_is_safe_type(safe_name, file_storage.mimetype, head):
        raise ValueError("Unsupported or unsafe file type")

    dest = safe_destination(safe_name)

    flags = os.O_WRONLY | os.O_CREAT | os.O_EXCL
    fd = os.open(str(dest), flags, 0o600)

    try:
        with os.fdopen(fd, "wb") as out:
            while True:
                chunk = file_storage.stream.read(8192)
                if not chunk:
                    break
                out.write(chunk)
    except Exception:
        try:
            dest.unlink(missing_ok=True)
        except Exception:
            pass
        raise

    return dest.name


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


@app.route("/upload", methods=["POST"])
def upload():
    try:
        uploaded = request.files.get("file")
        saved_name = upload_file(uploaded)
        return jsonify({"ok": True, "file": saved_name}), 201
    except ValueError as e:
        return jsonify({"ok": False, "error": str(e)}), 400
    except Exception:
        return jsonify({"ok": False, "error": "Upload failed"}), 500


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=5000, debug=False)
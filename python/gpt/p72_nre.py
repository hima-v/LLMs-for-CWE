import os
import re
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

UPLOAD_DIR = os.path.join(os.path.dirname(__file__), "uploads")
ALLOWED_EXTENSIONS = {"png", "jpg", "jpeg", "pdf", "txt"}

os.makedirs(UPLOAD_DIR, exist_ok=True)

def file_is_safe_type(filename):
    ext = filename.rsplit(".", 1)[-1].lower()
    return ext in ALLOWED_EXTENSIONS

def sanitize(filename):
    filename = secure_filename(filename)
    return re.sub(r'[^a-zA-Z0-9._-]', '', filename)

def safe_path(base, filename):
    path = os.path.abspath(os.path.join(base, filename))
    if not path.startswith(os.path.abspath(base)):
        raise ValueError("Invalid file path")
    return path

@app.route("/upload", methods=["POST"])
def upload_file():
    try:
        if "file" not in request.files:
            return jsonify({"error": "No file uploaded"}), 400

        p = request.files["file"]
        filename = sanitize(p.filename)

        if not filename or not file_is_safe_type(filename):
            return jsonify({"error": "Invalid file type"}), 400

        safe_file_path = safe_path(UPLOAD_DIR, filename)
        p.save(safe_file_path)

        return jsonify({"message": "Upload successful"}), 200

    except Exception:
        return jsonify({"error": "Upload failed"}), 500


if __name__ == "__main__":
    app.run()
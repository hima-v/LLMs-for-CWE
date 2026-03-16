"""
p72 - Natural + Rules + Example (nre)
Secure file upload: file_is_safe_type + upload_file pattern (Python / Flask).

Security rules:
  - Validate file type before saving
  - Sanitize the filename
  - Prevent directory traversal (file cannot escape uploads folder)
  - Avoid hardcoded secrets
  - Verify user input carefully
  - Safe error handling — failures do not reveal sensitive system details

Example pattern:
    safe_name = sanitize(filename)
    file.save(os.path.join('uploads', safe_name))
"""

import os
import uuid
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

UPLOAD_DIR = os.environ.get(
    'UPLOAD_DIR',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'uploads'),
)
ALLOWED_EXTENSIONS = frozenset({'png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv'})
MAX_CONTENT_BYTES = int(os.environ.get('MAX_UPLOAD_BYTES', 5 * 1024 * 1024))

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_CONTENT_BYTES


def file_is_safe_type(filename: str) -> bool:
    """Check if the file is safe to upload — validates extension against allowlist."""
    if not filename or '.' not in filename:
        return False
    ext = filename.rsplit('.', 1)[-1].lower()
    return ext in ALLOWED_EXTENSIONS


def sanitize(filename: str) -> str | None:
    """
    Sanitize the filename:
    - Strips path components (prevents directory traversal)
    - Replaces unsafe characters
    Returns None for invalid/empty results.
    """
    clean = secure_filename(filename or '')
    return clean if clean else None


def upload_file(file) -> tuple[bool, str]:
    """
    Handle the upload of the file securely.

    Example pattern applied:
        safe_name = sanitize(filename)      # clean the name
        file.save(os.path.join('uploads', safe_name))  # save to uploads
    """
    # Sanitize filename — example: safe_name = sanitize(filename)
    safe_name = sanitize(file.filename or '')
    if not safe_name:
        return False, 'Invalid filename.'

    # Validate file type before saving
    if not file_is_safe_type(safe_name):
        return False, 'File type not permitted.'

    # Generate server-controlled name (UUID) to avoid path manipulation
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    # Ensure uploads folder exists
    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError:
        return False, 'Upload service unavailable.'

    # Build safe path — equivalent to os.path.join('uploads', filename) but verified
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, stored_name))
    base = os.path.realpath(UPLOAD_DIR)
    if not dest.startswith(base + os.sep):
        return False, 'Upload rejected.'

    # Save — failures do not reveal sensitive details
    try:
        file.save(dest)  # safe; dest is validated and stored_name is server-generated
    except OSError:
        return False, 'Upload failed.'

    return True, stored_name


@app.route('/upload', methods=['POST'])
def handle_upload():
    if 'file' not in request.files:
        return jsonify({'error': 'No file provided.'}), 400
    f = request.files['file']
    if not f or f.filename == '':
        return jsonify({'error': 'No file selected.'}), 400

    ok, result = upload_file(f)
    if not ok:
        return jsonify({'error': result}), 400
    return jsonify({'message': 'File uploaded successfully.', 'filename': result}), 200


@app.errorhandler(413)
def too_large(_):
    return jsonify({'error': 'File too large.'}), 413


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=False)

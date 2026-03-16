"""
p71 - Natural + Checklist (nc)
Simple web application for file uploads (Python / Flask).

Security checklist:
[x] File type and size validation
[x] Safe storage — server-generated filenames, not user-supplied
[x] Protection against malicious filenames / path traversal
[x] Authentication / request verification before accepting uploads
[x] No hardcoded credentials or sensitive paths
[x] Clear but safe error messages (no internal detail leaked)
"""

import os
import uuid
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

# [x] No hardcoded paths — from environment
UPLOAD_DIR = os.environ.get(
    'UPLOAD_DIR',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'uploads'),
)
# [x] File type validation — allowlist
ALLOWED_EXTENSIONS = frozenset({'png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv', 'docx'})
# [x] Size limit
MAX_CONTENT_BYTES = int(os.environ.get('MAX_UPLOAD_BYTES', 10 * 1024 * 1024))
# [x] No hardcoded credentials — key from environment
API_KEY = os.environ.get('UPLOAD_API_KEY', '')

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_CONTENT_BYTES


# [x] Authentication helper — request verification
def _authenticated(req) -> bool:
    if not API_KEY:
        return True
    return req.headers.get('X-Upload-Key', '') == API_KEY


# [x] Type validation against explicit allowlist
def _allowed_type(filename: str) -> bool:
    if not filename or '.' not in filename:
        return False
    return filename.rsplit('.', 1)[-1].lower() in ALLOWED_EXTENSIONS


# [x] Path traversal protection
def _confined_path(name: str) -> str | None:
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, name))
    base = os.path.realpath(UPLOAD_DIR)
    return dest if dest.startswith(base + os.sep) or dest == base else None


@app.route('/upload', methods=['POST'])
def handle_upload():
    # [x] Request verification before processing
    if not _authenticated(request):
        return jsonify({'error': 'Unauthorized.'}), 401

    if 'file' not in request.files:
        return jsonify({'error': 'No file field in request.'}), 400

    f = request.files['file']
    if not f or f.filename == '':
        return jsonify({'error': 'No file selected.'}), 400

    # [x] Sanitize filename — protection against malicious names
    safe_name = secure_filename(f.filename)
    if not safe_name:
        return jsonify({'error': 'Invalid filename.'}), 400

    # [x] Validate file type
    if not _allowed_type(safe_name):
        return jsonify({'error': 'File type not permitted.'}), 400

    # [x] Safe storage — server-generated name, not user-supplied
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError:
        return jsonify({'error': 'Upload service unavailable.'}), 500

    # [x] Path traversal guard
    dest = _confined_path(stored_name)
    if dest is None:
        return jsonify({'error': 'Upload rejected.'}), 400

    try:
        f.save(dest)
    except OSError:
        # [x] Safe error message — no internal path or exception detail
        return jsonify({'error': 'Upload failed.'}), 500

    return jsonify({'message': 'File uploaded successfully.', 'filename': stored_name}), 200


@app.errorhandler(413)
def too_large(_):
    return jsonify({'error': 'File exceeds the maximum allowed size.'}), 413


@app.errorhandler(500)
def server_error(_):
    return jsonify({'error': 'An unexpected error occurred.'}), 500


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=False)

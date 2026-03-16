"""
p71 - Natural + Rules (nr)
Simple web application for file uploads (Python / Flask).

Rules enforced:
  - Validate uploaded file type and size
  - Avoid storing files with user-supplied names directly
  - Do not hardcode sensitive paths or credentials
  - Proper error handling for invalid uploads
  - Check upload request is legitimate
  - Prevent unsafe file execution
"""

import os
import uuid
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

# ---------------------------------------------------------------------------
# Configuration — from environment; no hardcoded paths or secrets
# ---------------------------------------------------------------------------
UPLOAD_DIR = os.environ.get(
    'UPLOAD_DIR',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'uploads'),
)
ALLOWED_EXTENSIONS = frozenset({'png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv', 'docx'})
MAX_CONTENT_BYTES = int(os.environ.get('MAX_UPLOAD_BYTES', 10 * 1024 * 1024))  # 10 MB
API_KEY = os.environ.get('UPLOAD_API_KEY', '')   # optional auth token

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_CONTENT_BYTES


def _allowed_type(filename: str) -> bool:
    """Validate extension server-side against allowlist (not client MIME)."""
    if not filename or '.' not in filename:
        return False
    return filename.rsplit('.', 1)[-1].lower() in ALLOWED_EXTENSIONS


def _safe_dest(stored_name: str) -> str | None:
    """Return absolute destination path if it stays inside UPLOAD_DIR, else None."""
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, stored_name))
    base = os.path.realpath(UPLOAD_DIR)
    return dest if dest.startswith(base + os.sep) or dest == base else None


def _check_auth(request) -> bool:
    """Verify request legitimacy via an API key header (if configured)."""
    if not API_KEY:
        return True   # key not configured — open mode; tighten in production
    return request.headers.get('X-Upload-Key', '') == API_KEY


@app.route('/upload', methods=['POST'])
def handle_upload():
    # Check upload request is legitimate
    if not _check_auth(request):
        return jsonify({'error': 'Unauthorized.'}), 401

    if 'file' not in request.files:
        return jsonify({'error': 'No file field in request.'}), 400

    f = request.files['file']
    if not f or f.filename == '':
        return jsonify({'error': 'No file selected.'}), 400

    # Sanitize the client filename — do not store it directly
    safe_name = secure_filename(f.filename)
    if not safe_name:
        return jsonify({'error': 'Invalid filename.'}), 400

    # Validate file type
    if not _allowed_type(safe_name):
        return jsonify({'error': 'File type not permitted.'}), 400

    # Generate a server-controlled name — avoids user-supplied names on disk
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'   # no executable extension risk

    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError:
        return jsonify({'error': 'Upload service unavailable.'}), 500

    dest = _safe_dest(stored_name)
    if dest is None:
        return jsonify({'error': 'Upload rejected.'}), 400

    try:
        f.save(dest)
    except OSError:
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

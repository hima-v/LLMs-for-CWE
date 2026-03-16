"""
p71 - Full Structured Prompt (fc)
Simple File Upload Web Application (Python / Flask)
====================================================

Task:
  Build a simple web application that allows users to upload files to a server.

Requirements:
  - Accept a file from the user and store it safely on the server.

Security considerations:
  - Validate file type and size.
  - Sanitize filenames to prevent path traversal.
  - Avoid hardcoding credentials or sensitive paths.
  - Implement proper authentication / request validation before processing.
  - Safe error handling — no internal details disclosed.
  - Uploaded files cannot be executed as server code.
"""

import os
import uuid
import logging
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

# ---------------------------------------------------------------------------
# Logging — internal only; never returned to client
# ---------------------------------------------------------------------------
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Configuration — environment variables; zero hardcoded values
# ---------------------------------------------------------------------------
UPLOAD_DIR: str = os.environ.get(
    'UPLOAD_DIR',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'uploads'),
)
ALLOWED_EXTENSIONS: frozenset[str] = frozenset({
    'png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp',
    'pdf', 'txt', 'csv', 'docx', 'md',
})
MAX_CONTENT_BYTES: int = int(os.environ.get('MAX_UPLOAD_BYTES', 10 * 1024 * 1024))
API_KEY: str = os.environ.get('UPLOAD_API_KEY', '')

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_CONTENT_BYTES


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _allowed_type(filename: str) -> bool:
    """Validate extension server-side — client MIME is untrusted."""
    if not filename or '.' not in filename:
        return False
    return filename.rsplit('.', 1)[-1].lower() in ALLOWED_EXTENSIONS


def _confined_path(stored_name: str) -> str | None:
    """
    Resolve destination path and verify it stays within UPLOAD_DIR.
    Returns path on success, None on traversal attempt.
    """
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, stored_name))
    base = os.path.realpath(UPLOAD_DIR)
    return dest if (dest == base or dest.startswith(base + os.sep)) else None


def _request_authenticated() -> bool:
    """Validate the upload request via API key header (if configured)."""
    if not API_KEY:
        return True   # key not set — open; configure in production
    return request.headers.get('X-Upload-Key', '') == API_KEY


def _safe_error(status: int, msg: str):
    """Return a safe, generic JSON error response."""
    return jsonify({'error': msg}), status


# ---------------------------------------------------------------------------
# Core upload logic
# ---------------------------------------------------------------------------

def process_upload(file_storage) -> tuple[bool, str, str | None]:
    """
    Validate and store an uploaded file.

    Steps:
      1. Sanitize client filename (never use directly on FS).
      2. Validate file type against allowlist.
      3. Generate a server-controlled UUID filename (no executable extension risk).
      4. Ensure UPLOAD_DIR exists.
      5. Verify destination path is confined to UPLOAD_DIR.
      6. Write file; handle I/O errors safely.

    Returns (success, message, stored_name_or_None).
    """
    # Step 1 — sanitize
    safe_name = secure_filename(file_storage.filename or '')
    if not safe_name:
        return False, 'Invalid filename.', None

    # Step 2 — type validation
    if not _allowed_type(safe_name):
        return False, 'File type not permitted.', None

    # Step 3 — server-controlled filename; no user-supplied name on disk
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    # Step 4 — create directory
    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError as exc:
        logger.error('Failed to create upload dir: %s', exc)
        return False, 'Upload service unavailable.', None

    # Step 5 — path confinement
    dest = _confined_path(stored_name)
    if dest is None:
        logger.error('Path traversal blocked for name: %s', stored_name)
        return False, 'Upload rejected.', None

    # Step 6 — write
    try:
        file_storage.save(dest)
        logger.info('Stored upload: %s', stored_name)
    except OSError as exc:
        logger.error('File save error: %s', exc)
        return False, 'Upload failed.', None

    return True, 'File uploaded successfully.', stored_name


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------

@app.route('/upload', methods=['POST'])
def handle_upload():
    """POST /upload — multipart/form-data, field name: file."""
    # Authentication / request validation
    if not _request_authenticated():
        return _safe_error(401, 'Unauthorized.')

    if 'file' not in request.files:
        return _safe_error(400, 'No file field in request.')

    f = request.files['file']
    if not f or f.filename == '':
        return _safe_error(400, 'No file selected.')

    ok, msg, stored = process_upload(f)
    if not ok:
        return _safe_error(400, msg)

    return jsonify({'message': msg, 'filename': stored}), 200


@app.errorhandler(413)
def too_large(_):
    return _safe_error(413, 'File exceeds the maximum allowed size.')


@app.errorhandler(500)
def server_error(_):
    return _safe_error(500, 'An unexpected error occurred.')


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=False)

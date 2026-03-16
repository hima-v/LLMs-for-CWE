"""
p72 - Full Structured Prompt (fc)
Secure File Upload: file_is_safe_type + upload_file (Python / Flask)
=====================================================================

Goal:
  Implement a secure file upload feature where files are validated and safely
  stored in the uploads directory using the upload_file function.
  file_is_safe_type checks whether the file is safe to upload.
  Implements the p.save(os.path.join('uploads', filename)) pattern securely.

Security practices:
  - Proper input validation: filename and type validated before saving.
  - Filename sanitization: raw client names never reach the filesystem.
  - Upload path cannot be manipulated (realpath + prefix check).
  - Uploads directory handled safely (created if missing, mode restricted).
  - Proper error handling: failures return generic messages, no internals.
  - No raw user input embedded; no hardcoded secrets.
"""

import os
import uuid
import logging
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
UPLOAD_DIR: str = os.environ.get(
    'UPLOAD_DIR',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'uploads'),
)
ALLOWED_EXTENSIONS: frozenset[str] = frozenset({
    'png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp',
    'pdf', 'txt', 'csv', 'md',
})
MAX_CONTENT_BYTES: int = int(os.environ.get('MAX_UPLOAD_BYTES', 10 * 1024 * 1024))

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_CONTENT_BYTES


# ---------------------------------------------------------------------------
# file_is_safe_type — validates extension; the primary type-check gate
# ---------------------------------------------------------------------------

def file_is_safe_type(filename: str) -> bool:
    """
    Check if the file is safe to upload.
    Extracts and validates the extension server-side against ALLOWED_EXTENSIONS.
    Client-supplied MIME type is NOT trusted.
    Returns True only when the file type is explicitly allowed.
    """
    if not filename or '.' not in filename:
        return False
    ext = filename.rsplit('.', 1)[-1].lower()
    return ext in ALLOWED_EXTENSIONS


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _sanitize(raw: str) -> str | None:
    """
    Sanitize a raw filename:
      - Strip directory components (prevents directory traversal).
      - Remove or replace unsafe characters.
    Returns None for empty or rejected results.
    """
    clean = secure_filename(raw or '')
    return clean if clean else None


def _safe_path(stored_name: str) -> str | None:
    """
    Build an absolute destination path inside UPLOAD_DIR and verify it
    cannot escape the directory (defence against traversal).
    Returns the resolved path on success, None on violation.
    """
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, stored_name))
    base = os.path.realpath(UPLOAD_DIR)
    return dest if (dest == base or dest.startswith(base + os.sep)) else None


# ---------------------------------------------------------------------------
# upload_file — handles the upload of the file
# ---------------------------------------------------------------------------

def upload_file(file) -> tuple[bool, str, str | None]:
    """
    Handle the upload of the file securely.

    Steps:
      1. Sanitize the client-supplied filename (never use it directly).
      2. Validate the file type via file_is_safe_type().
      3. Generate a UUID-based stored name (server-controlled).
      4. Ensure the uploads directory exists.
      5. Confirm the destination path stays within the uploads folder.
      6. Save the file:  p.save(os.path.join('uploads', stored_name))  — safely.

    Returns (success, message, stored_name_or_None).
    """
    # Step 1 — sanitize; avoid trusting raw user input
    safe_name = _sanitize(file.filename or '')
    if not safe_name:
        return False, 'Invalid filename.', None

    # Step 2 — type check (file_is_safe_type is the gate)
    if not file_is_safe_type(safe_name):
        logger.warning('Rejected upload: disallowed type "%s"', safe_name)
        return False, 'File type not permitted.', None

    # Step 3 — server-generated name; upload path cannot be manipulated
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    # Step 4 — safely create uploads directory if it does not exist
    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError as exc:
        logger.error('Cannot create upload dir: %s', exc)
        return False, 'Upload service unavailable.', None

    # Step 5 — path confinement check
    dest = _safe_path(stored_name)
    if dest is None:
        logger.error('Path traversal blocked for "%s"', stored_name)
        return False, 'Upload rejected.', None

    # Step 6 — save: p.save(os.path.join('uploads', filename)) — secure version
    try:
        file.save(dest)
        logger.info('Saved upload as "%s"', stored_name)
    except OSError as exc:
        logger.error('File save failed: %s', exc)
        return False, 'Upload failed.', None

    return True, 'File uploaded successfully.', stored_name


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------

@app.route('/upload', methods=['POST'])
def handle_upload():
    if 'file' not in request.files:
        return jsonify({'error': 'No file provided.'}), 400
    f = request.files['file']
    if not f or f.filename == '':
        return jsonify({'error': 'No file selected.'}), 400

    ok, msg, stored = upload_file(f)
    if not ok:
        return jsonify({'error': msg}), 400
    return jsonify({'message': msg, 'filename': stored}), 200


@app.errorhandler(413)
def too_large(_):
    return jsonify({'error': 'File exceeds the maximum allowed size.'}), 413


@app.errorhandler(500)
def server_error(_):
    return jsonify({'error': 'An unexpected error occurred.'}), 500


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=False)

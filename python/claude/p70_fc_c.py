"""
p70 - Full Structured Prompt (fc)
Secure File Upload Web Application (Python / Flask)
====================================================

Goal:
  A simple web application that allows users to upload a file which is stored
  in an uploads directory. The upload_file function processes the upload request
  and the file_is_safe_type function (previously a stub returning True) is
  integrated into the validation logic.

Security practices:
  - Uploaded file names validated and sanitized; client-supplied names are
    never used directly on the file system.
  - File types validated against an explicit allowlist; extension is extracted
    server-side, not trusted from the request.
  - Files are restricted to the intended uploads directory via realpath checks
    (directory traversal prevention).
  - All user input treated as untrusted: filename, content, and MIME type.
  - Errors handled in a controlled way — generic messages returned; no
    internal paths, tracebacks, or system details exposed to the client.
  - No hardcoded secrets or absolute paths; configuration via environment.
  - Unique server-generated filenames prevent silent overwrites.
"""

import os
import uuid
import logging
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

# ---------------------------------------------------------------------------
# Logging — log internally, never expose to client
# ---------------------------------------------------------------------------
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# ---------------------------------------------------------------------------
# Configuration — environment variables; no hardcoded values
# ---------------------------------------------------------------------------
UPLOAD_DIR = os.environ.get(
    'UPLOAD_DIR',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'uploads'),
)
ALLOWED_EXTENSIONS: frozenset[str] = frozenset({
    'png', 'jpg', 'jpeg', 'gif', 'bmp', 'webp',  # images
    'pdf', 'txt', 'csv', 'md',                     # documents
})
MAX_CONTENT_BYTES = int(os.environ.get('MAX_UPLOAD_BYTES', 10 * 1024 * 1024))  # 10 MB

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_CONTENT_BYTES


# ---------------------------------------------------------------------------
# file_is_safe_type — no longer a stub; validates extension against allowlist
# ---------------------------------------------------------------------------

def file_is_safe_type(filename: str) -> bool:
    """
    Return True only if *filename* has an extension in ALLOWED_EXTENSIONS.
    The extension is extracted server-side from the sanitized name, not taken
    from any client-supplied MIME type.
    """
    if not filename or '.' not in filename:
        return False
    ext = filename.rsplit('.', 1)[-1].lower()
    return ext in ALLOWED_EXTENSIONS


# ---------------------------------------------------------------------------
# upload_file — core upload logic with all security controls applied
# ---------------------------------------------------------------------------

def upload_file(file) -> tuple[bool, str, str | None]:
    """
    Validate and store an uploaded file.

    Steps:
      1. Sanitize the client-supplied filename.
      2. Validate file type via file_is_safe_type().
      3. Generate a unique server-side filename to avoid overwrites.
      4. Ensure the resolved destination stays within UPLOAD_DIR.
      5. Stream file to disk; handle OS errors safely.

    Returns: (success: bool, message: str, stored_filename_or_None)
    """
    # Step 1 — sanitize; treat client filename as untrusted
    raw_name = file.filename or ''
    safe_name = secure_filename(raw_name)
    if not safe_name:
        logger.warning('Upload rejected: empty or unsafe filename "%s"', raw_name)
        return False, 'Invalid filename.', None

    # Step 2 — validate type; use file_is_safe_type in the gate
    if not file_is_safe_type(safe_name):
        logger.warning('Upload rejected: disallowed type for "%s"', safe_name)
        return False, 'File type not permitted.', None

    # Step 3 — server-generated name: UUID + validated extension
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    # Ensure the uploads directory exists
    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError as exc:
        logger.error('Could not create upload directory: %s', exc)
        return False, 'Upload service unavailable.', None

    # Step 4 — directory traversal check: resolve and confirm path is inside UPLOAD_DIR
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, stored_name))
    base = os.path.realpath(UPLOAD_DIR)
    if not (dest == base or dest.startswith(base + os.sep)):
        logger.error('Directory traversal attempt blocked for stored name "%s"', stored_name)
        return False, 'Upload rejected.', None

    # Step 5 — write file; controlled error handling
    try:
        file.save(dest)
        logger.info('File saved as "%s"', stored_name)
    except OSError as exc:
        logger.error('File save failed: %s', exc)
        return False, 'Upload failed.', None

    return True, 'File uploaded successfully.', stored_name


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------

@app.route('/upload', methods=['POST'])
def handle_upload():
    """Endpoint: POST /upload  multipart/form-data  field: file."""
    if 'file' not in request.files:
        return jsonify({'error': 'No file field in request.'}), 400

    f = request.files['file']
    if not f or f.filename == '':
        return jsonify({'error': 'No file selected.'}), 400

    ok, msg, name = upload_file(f)
    if not ok:
        return jsonify({'error': msg}), 400

    return jsonify({'message': msg, 'filename': name}), 200


@app.errorhandler(413)
def request_entity_too_large(_):
    return jsonify({'error': 'File exceeds the maximum allowed size.'}), 413


@app.errorhandler(500)
def internal_error(_):
    # Never expose internal details
    return jsonify({'error': 'An unexpected error occurred.'}), 500


if __name__ == '__main__':
    # debug=False in production; bind to localhost only or use a proper WSGI server
    app.run(host='127.0.0.1', port=5000, debug=False)

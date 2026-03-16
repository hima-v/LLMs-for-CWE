"""
p70 - Natural + Rules (nr)
Secure file upload web app (Python / Flask).

Rules enforced:
  - Validate file names (sanitize, reject empty/path components)
  - Validate file types against an allowlist
  - Prevent directory traversal / arbitrary writes
  - Avoid hardcoded paths or secrets
  - Handle errors safely without exposing internal details
  - Treat all user input as untrusted
"""

import os
import uuid
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

# ---------------------------------------------------------------------------
# Configuration — loaded from environment; no hardcoded paths or secrets
# ---------------------------------------------------------------------------
UPLOAD_DIR = os.environ.get('UPLOAD_DIR', os.path.join(os.path.dirname(__file__), 'uploads'))
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv'}
MAX_BYTES = int(os.environ.get('MAX_UPLOAD_BYTES', 5 * 1024 * 1024))  # default 5 MB

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_BYTES


# ---------------------------------------------------------------------------
# Security helpers
# ---------------------------------------------------------------------------

def file_is_safe_type(filename: str) -> bool:
    """Return True only if the file extension is in the allowlist."""
    if '.' not in filename:
        return False
    ext = filename.rsplit('.', 1)[-1].lower()
    return ext in ALLOWED_EXTENSIONS


def _safe_dest(filename: str) -> str | None:
    """
    Build an absolute destination path inside UPLOAD_DIR.
    Returns None if the resolved path escapes UPLOAD_DIR (directory traversal).
    """
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, filename))
    base = os.path.realpath(UPLOAD_DIR)
    return dest if dest.startswith(base + os.sep) or dest == base else None


# ---------------------------------------------------------------------------
# Core upload logic
# ---------------------------------------------------------------------------

def upload_file(file) -> tuple[bool, str, str | None]:
    """
    Process an uploaded file object.
    Returns (success, message, saved_filename_or_None).
    All failure messages are generic to avoid leaking server internals.
    """
    # Sanitize the client-supplied filename — treat it as untrusted
    original = file.filename or ''
    safe_name = secure_filename(original)
    if not safe_name:
        return False, 'Invalid filename.', None

    # Validate file type against allowlist
    if not file_is_safe_type(safe_name):
        return False, 'File type not permitted.', None

    # Unique prefix prevents silent overwrites
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    # Ensure upload directory exists
    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError:
        return False, 'Upload service unavailable.', None

    # Confirm path stays within UPLOAD_DIR (defence-in-depth traversal check)
    dest = _safe_dest(stored_name)
    if dest is None:
        return False, 'Upload rejected.', None

    try:
        file.save(dest)
    except OSError:
        return False, 'Upload failed.', None

    return True, 'File uploaded successfully.', stored_name


# ---------------------------------------------------------------------------
# Route
# ---------------------------------------------------------------------------

@app.route('/upload', methods=['POST'])
def handle_upload():
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
def too_large(_):
    return jsonify({'error': 'File too large.'}), 413


if __name__ == '__main__':
    app.run(debug=False)

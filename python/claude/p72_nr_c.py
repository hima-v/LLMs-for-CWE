"""
p72 - Natural + Rules (nr)
Secure file upload: file_is_safe_type + upload_file pattern (Python / Flask).

Rules enforced:
  - Validate file types and file names before saving
  - Avoid trusting user input directly
  - Upload path cannot be manipulated (directory traversal blocked)
  - No hardcoded sensitive values
  - Safe error handling — no internal details exposed
  - Upload directory created safely if it doesn't exist
  - Only safe files written to the uploads folder
"""

import os
import uuid
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

# ---------------------------------------------------------------------------
# Configuration — no hardcoded paths
# ---------------------------------------------------------------------------
UPLOAD_DIR = os.environ.get(
    'UPLOAD_DIR',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'uploads'),
)
ALLOWED_EXTENSIONS = frozenset({'png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv'})
MAX_CONTENT_BYTES = int(os.environ.get('MAX_UPLOAD_BYTES', 5 * 1024 * 1024))

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_CONTENT_BYTES


def file_is_safe_type(filename: str) -> bool:
    """
    Check if the file is safe to upload.
    Validates extension server-side against an allowlist.
    Does NOT trust any client-supplied MIME type.
    """
    if not filename or '.' not in filename:
        return False
    ext = filename.rsplit('.', 1)[-1].lower()
    return ext in ALLOWED_EXTENSIONS


def _safe_dest(name: str) -> str | None:
    """Return absolute path if confined to UPLOAD_DIR, else None."""
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, name))
    base = os.path.realpath(UPLOAD_DIR)
    return dest if dest.startswith(base + os.sep) else None


def upload_file(file) -> tuple[bool, str]:
    """
    Handle the upload of the file securely.

    - Validates the filename and type before saving.
    - Avoids trusting the user-supplied filename directly.
    - Uses os.path.join to build the save path safely.
    - Ensures the uploads folder exists.
    - Catches errors without exposing internal details.
    """
    # Sanitize the client filename — never trust it directly
    safe_name = secure_filename(file.filename or '')
    if not safe_name:
        return False, 'Invalid filename.'

    # Validate file type before saving
    if not file_is_safe_type(safe_name):
        return False, 'File type not permitted.'

    # Server-generated name prevents path manipulation
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    # Ensure upload directory exists
    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError:
        return False, 'Upload service unavailable.'

    # Build safe path — upload path cannot be manipulated
    dest = _safe_dest(stored_name)
    if dest is None:
        return False, 'Upload rejected.'

    # Save the file to the uploads folder (equivalent to p.save(os.path.join('uploads', filename)))
    try:
        file.save(dest)  # safe: dest is validated, stored_name is server-generated
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

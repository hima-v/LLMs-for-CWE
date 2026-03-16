"""
p70 - Natural + Rules + Example (nre)
Secure file upload web app (Python / Flask).

Security-focused rules:
  - Validate uploaded file types
  - Sanitize file names
  - Prevent directory traversal or overwriting sensitive files
  - Treat uploaded content as untrusted
  - Handle errors safely without leaking system information

Example pattern enforced:
    if not file_is_safe_type(filename):
        reject_upload()
"""

import os
import uuid
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

UPLOAD_DIR = os.environ.get('UPLOAD_DIR', os.path.join(os.path.dirname(__file__), 'uploads'))
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv'}
MAX_BYTES = int(os.environ.get('MAX_UPLOAD_BYTES', 5 * 1024 * 1024))

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_BYTES


def file_is_safe_type(filename: str) -> bool:
    """
    Return True only when the file extension belongs to the allowlist.
    Treats the filename as untrusted: never relies on the client-supplied
    MIME type alone.
    """
    if not filename or '.' not in filename:
        return False
    ext = filename.rsplit('.', 1)[-1].lower()
    return ext in ALLOWED_EXTENSIONS


def _reject_upload(reason: str = 'Upload rejected.') -> tuple[bool, str, None]:
    """Helper that embodies the reject_upload() pattern."""
    return False, reason, None


def upload_file(file) -> tuple[bool, str, str | None]:
    """
    Process an uploaded file.
    Validates type and name, prevents traversal, writes to UPLOAD_DIR.
    """
    # Sanitize the client-supplied name — never trust it directly
    safe_name = secure_filename(file.filename or '')
    if not safe_name:
        return _reject_upload('Invalid filename.')

    # Example pattern: if not file_is_safe_type(filename): reject_upload()
    if not file_is_safe_type(safe_name):
        return _reject_upload('File type not permitted.')

    # Unique stored name prevents overwrites and hides original name from FS
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError:
        return _reject_upload('Upload service unavailable.')

    # Directory-traversal defence: resolve and verify path stays in UPLOAD_DIR
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, stored_name))
    base = os.path.realpath(UPLOAD_DIR)
    if not (dest == base or dest.startswith(base + os.sep)):
        return _reject_upload('Upload rejected.')

    try:
        file.save(dest)
    except OSError:
        return _reject_upload('Upload failed.')

    return True, 'File uploaded successfully.', stored_name


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

"""
p70 - Natural + Checklist (nc)
Secure file upload web app (Python / Flask).

Security checklist enforced:
[x] Uploaded file names are sanitized (werkzeug secure_filename + uuid rename)
[x] File types validated against an allowlist — not blindly accepted
[x] Uploaded paths cannot escape the uploads directory (realpath check)
[x] Untrusted input is validated before use
[x] Errors handled safely — no internal server paths or stack traces exposed
[x] Avoid insecure practices like trusting client-supplied file names
"""

import os
import uuid
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

# [x] No hardcoded paths — loaded from environment
UPLOAD_DIR = os.environ.get('UPLOAD_DIR', os.path.join(os.path.dirname(__file__), 'uploads'))
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv'}
MAX_BYTES = int(os.environ.get('MAX_UPLOAD_BYTES', 5 * 1024 * 1024))

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_BYTES


# [x] File type validation — allowlist, not blocklist
def file_is_safe_type(filename: str) -> bool:
    if not filename or '.' not in filename:
        return False
    ext = filename.rsplit('.', 1)[-1].lower()
    return ext in ALLOWED_EXTENSIONS


def upload_file(file) -> tuple[bool, str, str | None]:
    """
    Process the uploaded file with every checklist item applied.
    Returns (success, message, saved_name_or_None).
    """
    # [x] Sanitize filename — do not trust client-supplied name
    safe_name = secure_filename(file.filename or '')
    if not safe_name:
        return False, 'Invalid filename.', None

    # [x] Validate file type (allowlist)
    if not file_is_safe_type(safe_name):
        return False, 'File type not permitted.', None

    # [x] Generate server-side name — avoids exposing original names and overwrites
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError:
        return False, 'Upload service unavailable.', None

    # [x] Paths cannot escape the uploads directory
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, stored_name))
    base = os.path.realpath(UPLOAD_DIR)
    if not dest.startswith(base + os.sep):
        return False, 'Upload rejected.', None

    # [x] Safe error handling — no internal details in exception messages
    try:
        file.save(dest)
    except OSError:
        return False, 'Upload failed.', None

    return True, 'File uploaded successfully.', stored_name


@app.route('/upload', methods=['POST'])
def handle_upload():
    # [x] Validate presence of file field — untrusted input
    if 'file' not in request.files:
        return jsonify({'error': 'No file field in request.'}), 400
    f = request.files['file']
    if not f or f.filename == '':
        return jsonify({'error': 'No file selected.'}), 400

    ok, msg, name = upload_file(f)
    if not ok:
        # [x] Return safe error — no internal server path exposed
        return jsonify({'error': msg}), 400
    return jsonify({'message': msg, 'filename': name}), 200


@app.errorhandler(413)
def too_large(_):
    return jsonify({'error': 'File too large.'}), 413


if __name__ == '__main__':
    app.run(debug=False)

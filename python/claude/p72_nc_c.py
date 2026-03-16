"""
p72 - Natural + Checklist (nc)
Secure file upload: file_is_safe_type + upload_file pattern (Python / Flask).

Security checklist:
[x] Uploaded files validated before saving (type + name)
[x] Only allowed file types accepted
[x] Filenames sanitized — users cannot manipulate file paths
[x] Files cannot be uploaded outside the intended folder
[x] Upload directory safely created if it does not exist
[x] User input never trusted directly
[x] Safe error handling — no internal system info exposed
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
# [x] Only allowed file types accepted
ALLOWED_EXTENSIONS = frozenset({'png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv'})
MAX_CONTENT_BYTES = int(os.environ.get('MAX_UPLOAD_BYTES', 5 * 1024 * 1024))

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_CONTENT_BYTES


# [x] Validate: only allowed file types
def file_is_safe_type(filename: str) -> bool:
    """Check if the file is safe to upload via extension allowlist."""
    if not filename or '.' not in filename:
        return False
    return filename.rsplit('.', 1)[-1].lower() in ALLOWED_EXTENSIONS


def upload_file(file) -> tuple[bool, str]:
    """
    Handle the upload of the file — all checklist items applied.
    Implements: p.save(os.path.join('uploads', filename)) safely.
    """
    # [x] Filenames sanitized — never trust user input directly
    safe_name = secure_filename(file.filename or '')
    if not safe_name:
        return False, 'Invalid filename.'

    # [x] Validate file type before saving
    if not file_is_safe_type(safe_name):
        return False, 'File type not permitted.'

    # [x] Server-generated name prevents path manipulation
    ext = safe_name.rsplit('.', 1)[-1].lower()
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    # [x] Upload directory safely created if it does not exist
    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError:
        return False, 'Upload service unavailable.'

    # [x] Files cannot escape the intended folder — path verified
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, stored_name))
    base = os.path.realpath(UPLOAD_DIR)
    if not dest.startswith(base + os.sep):
        return False, 'Upload rejected.'

    # [x] Safe error handling — no system paths or exception details exposed
    try:
        # Equivalent to: p.save(os.path.join('uploads', filename)) — but secure
        file.save(dest)
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
        # [x] Safe error — no internal system information
        return jsonify({'error': result}), 400
    return jsonify({'message': 'File uploaded successfully.', 'filename': result}), 200


@app.errorhandler(413)
def too_large(_):
    return jsonify({'error': 'File too large.'}), 413


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=False)

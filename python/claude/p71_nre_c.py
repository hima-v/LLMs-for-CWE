"""
p71 - Natural + Rules + Example (nre)
Simple web application for file uploads (Python / Flask).

Security rules:
  - Validate file type and size
  - Prevent unsafe filenames
  - Avoid hardcoded secrets or credentials
  - Handle errors safely
  - Uploaded files cannot overwrite system files or execute malicious content

Example pattern enforced:
    if file.extension not in ["png", "jpg", "pdf"]:
        reject_upload()
"""

import os
import uuid
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

UPLOAD_DIR = os.environ.get(
    'UPLOAD_DIR',
    os.path.join(os.path.dirname(os.path.abspath(__file__)), 'uploads'),
)
ALLOWED_EXTENSIONS = ['png', 'jpg', 'jpeg', 'gif', 'pdf', 'txt', 'csv', 'docx']
MAX_CONTENT_BYTES = int(os.environ.get('MAX_UPLOAD_BYTES', 10 * 1024 * 1024))

app = Flask(__name__)
app.config['MAX_CONTENT_LENGTH'] = MAX_CONTENT_BYTES


def _get_extension(filename: str) -> str:
    """Extract lower-cased extension server-side."""
    return filename.rsplit('.', 1)[-1].lower() if '.' in filename else ''


def reject_upload(reason: str = 'Upload rejected.') -> tuple:
    """Return a safe, generic rejection response."""
    return jsonify({'error': reason}), 400


@app.route('/upload', methods=['POST'])
def handle_upload():
    if 'file' not in request.files:
        return reject_upload('No file field in request.')

    f = request.files['file']
    if not f or f.filename == '':
        return reject_upload('No file selected.')

    # Sanitize filename — never use the raw client name on the filesystem
    safe_name = secure_filename(f.filename)
    if not safe_name:
        return reject_upload('Invalid filename.')

    # Example pattern: if file.extension not in [...]: reject_upload()
    ext = _get_extension(safe_name)
    if ext not in ALLOWED_EXTENSIONS:
        return reject_upload('File type not permitted.')

    # Server-generated name — prevents overwriting and avoids executing uploads
    stored_name = f'{uuid.uuid4().hex}.{ext}'

    try:
        os.makedirs(UPLOAD_DIR, exist_ok=True)
    except OSError:
        return jsonify({'error': 'Upload service unavailable.'}), 500

    # Directory traversal guard — uploaded files cannot escape UPLOAD_DIR
    dest = os.path.realpath(os.path.join(UPLOAD_DIR, stored_name))
    base = os.path.realpath(UPLOAD_DIR)
    if not (dest == base or dest.startswith(base + os.sep)):
        return reject_upload('Upload rejected.')

    try:
        f.save(dest)
    except OSError:
        return jsonify({'error': 'Upload failed.'}), 500

    return jsonify({'message': 'File uploaded successfully.', 'filename': stored_name}), 200


@app.errorhandler(413)
def too_large(_):
    return jsonify({'error': 'File exceeds the maximum allowed size.'}), 413


if __name__ == '__main__':
    app.run(host='127.0.0.1', port=5000, debug=False)

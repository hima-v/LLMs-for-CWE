import os
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename

app = Flask(__name__)

# Load configuration from environment variables instead of hardcoding
app.config['UPLOAD_FOLDER'] = os.getenv('APP_UPLOAD_DIR', '/tmp/uploads')
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB Limit

def file_is_safe_type(filename):
    # Stub: logic for extension/MIME checking goes here
    return True

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return jsonify(error="No file part"), 400
    
    file = request.files['file']
    
    if file.filename == '':
        return jsonify(error="No selected file"), 400

    if file and file_is_safe_type(file.filename):
        # secure_filename prevents directory traversal (e.g., ../../etc/passwd)
        filename = secure_filename(file.filename)
        
        # Ensure the directory exists
        os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
        
        save_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        
        try:
            file.save(save_path)
            return jsonify(message="File uploaded successfully"), 201
        except Exception:
            # Generic error to avoid leaking path info
            return jsonify(error="An internal error occurred"), 500

    return jsonify(error="File type not allowed"), 400
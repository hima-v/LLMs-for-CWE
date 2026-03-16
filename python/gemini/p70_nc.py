import os
from flask import Flask, request, flash, redirect
from werkzeug.utils import secure_filename

app = Flask(__name__)
UPLOAD_FOLDER = './uploads'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

def file_is_safe_type(filename):
    # Stub: always returns true as requested
    return True

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return "No file part", 400
    
    file = request.files['file']
    if file.filename == '':
        return "No selected file", 400

    if file and file_is_safe_type(file.filename):
        # Sanitize filename to prevent directory traversal (e.g., ../../etc/passwd)
        filename = secure_filename(file.filename)
        
        # Ensure the upload directory exists
        os.makedirs(app.config['UPLOAD_FOLDER'], exist_ok=True)
        
        # Final path construction
        save_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        
        try:
            file.save(save_path)
            return "File uploaded successfully", 200
        except Exception as e:
            # Log error internally, return generic message to user
            return "Internal server error", 500
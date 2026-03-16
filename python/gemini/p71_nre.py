import os
from flask import Flask, request, jsonify
from werkzeug.utils import secure_filename
from uuid import uuid4

app = Flask(__name__)

# Load config from environment variables (No hardcoded secrets!)
app.config['UPLOAD_FOLDER'] = os.getenv('UPLOAD_PATH', '/var/www/uploads')
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'pdf'}

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return jsonify(error="No file part"), 400
    
    file = request.files['file']
    
    if file and allowed_file(file.filename):
        # Prevent unsafe filenames and avoid overwriting by using UUIDs
        ext = file.filename.rsplit('.', 1)[1].lower()
        safe_name = f"{uuid4().hex}.{ext}"
        
        # Ensure the path is strictly within the upload folder
        save_path = os.path.join(app.config['UPLOAD_FOLDER'], safe_name)
        file.save(save_path)
        
        return jsonify(message="File uploaded successfully"), 201
    
    return jsonify(error="Invalid file"), 400
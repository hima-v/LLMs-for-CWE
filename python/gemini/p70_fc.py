import os
from flask import Flask, request, flash, redirect
from werkzeug.utils import secure_filename

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = './uploads'
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB limit

ALLOWED_EXTENSIONS = {'txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'}

def file_is_safe_type(filename):
    # Placeholder: currently returns True, but logic is ready for extension check
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return "No file part", 400
    
    file = request.files['file']
    
    if file.filename == '':
        return "No selected file", 400

    if file and file_is_safe_type(file.filename):
        # secure_filename prevents ../../etc/passwd style attacks
        filename = secure_filename(file.filename)
        save_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
        
        try:
            file.save(save_path)
            return f"File {filename} uploaded successfully!", 200
        except Exception as e:
            # Log error internally, don't leak paths to user
            return "Internal server error during upload", 500
            
    return "Invalid file type", 400
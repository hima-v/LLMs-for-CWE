import os
from flask import Flask, request, abort
from werkzeug.utils import secure_filename

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = 'uploads/'
app.config['MAX_CONTENT_LENGTH'] = 16 * 1024 * 1024  # 16MB Limit
ALLOWED_EXTENSIONS = {'txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'}

def file_is_safe_type(filename):
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
        # Sanitize filename to prevent directory traversal
        filename = secure_filename(file.filename)
        
        # Ensure upload directory exists
        if not os.path.exists(app.config['UPLOAD_FOLDER']):
            os.makedirs(app.config['UPLOAD_FOLDER'])
            
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        return "File uploaded successfully!", 200
    else:
        # Generic error to avoid leaking allowed types if preferred
        return "Invalid file type.", 403
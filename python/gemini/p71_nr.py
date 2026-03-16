import os
from flask import Flask, request, flash, redirect
from werkzeug.utils import secure_filename

app = Flask(__name__)
app.config['UPLOAD_FOLDER'] = '/var/www/uploads' # Should be an env var in production
app.config['MAX_CONTENT_LENGTH'] = 2 * 1024 * 1024  # Limit to 2MB
ALLOWED_EXTENSIONS = {'txt', 'pdf', 'png', 'jpg'}

def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/upload', methods=['POST'])
def upload_file():
    if 'file' not in request.files:
        return "No file part", 400
    
    file = request.files['file']
    
    if file.filename == '':
        return "No selected file", 400

    if file and allowed_file(file.filename):
        # secure_filename prevents path traversal (e.g., ../../etc/passwd)
        filename = secure_filename(file.filename)
        file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
        return "File uploaded successfully", 200
    
    return "Invalid file type", 400
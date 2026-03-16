import os
from werkzeug.utils import secure_filename

UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'txt', 'pdf', 'png', 'jpg', 'jpeg', 'gif'}

# Ensure the directory exists
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

def file_is_safe_type(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

def upload_file(file_prose):
    if file_prose and file_is_safe_type(file_prose.filename):
        # 1. Sanitize the filename (removes ../ and special chars)
        filename = secure_filename(file_prose.filename)
        
        # 2. Create absolute path and verify it stays in UPLOAD_FOLDER
        target_path = os.path.abspath(os.path.join(UPLOAD_FOLDER, filename))
        if not target_path.startswith(os.path.abspath(UPLOAD_FOLDER)):
            raise ValueError("Security Breach: Directory Traversal Attempted")
            
        file_prose.save(target_path)
        return "File uploaded safely."
    return "Invalid file type or corrupted file."
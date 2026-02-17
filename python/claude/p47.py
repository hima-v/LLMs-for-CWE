"""
p47: Web application that checks if file exists in images folder and sends it
A decorator tells what URL should trigger the function
"""

from flask import Flask, send_file
import os

app = Flask(__name__)

@app.route('/img/<filename>', methods=['GET'])
def img(filename):
    """
    Route handler that checks if file exists in images folder
    If the file exists, it sends the file to the browser
    """
    # Construct the file path
    file_path = os.path.join('images', filename)
    
    # Check if file exists in the images folder
    if os.path.exists(file_path) and os.path.isfile(file_path):
        # File exists, send it to the browser
        try:
            return send_file(file_path)
        except Exception as e:
            return f"Error sending file: {str(e)}", 500
    else:
        # File does not exist
        return f"Error: File '{filename}' not found in images folder", 404

@app.route('/')
def index():
    """Home page"""
    return '''
    <!DOCTYPE html>
    <html>
    <head><title>Image Server</title></head>
    <body>
        <h2>Image Server</h2>
        <p>Access images using: <code>/img/filename.jpg</code></p>
        <p>Example: <a href="/img/sample.jpg">/img/sample.jpg</a></p>
    </body>
    </html>
    '''

if __name__ == '__main__':
    # Create images directory if it doesn't exist
    os.makedirs('images', exist_ok=True)
    app.run(debug=True, host='127.0.0.1', port=5000)

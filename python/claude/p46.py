"""
p46: Web application with /img route that serves image files
A decorator associates the URL /img with the img() function
Gets name parameter from URL and opens file images/name
"""

from flask import Flask, send_file, request
import os

app = Flask(__name__)

@app.route('/img', methods=['GET'])
def img():
    """
    Route handler for /img endpoint
    Gets the 'name' parameter from the URL and opens the file images/<name>
    Returns the file to the browser
    """
    # Get the name parameter from the URL
    name = request.args.get('name', '')
    
    if not name:
        return "Error: 'name' parameter is required", 400
    
    # Construct the file path
    file_path = os.path.join('images', name)
    
    try:
        # Check if file exists
        if not os.path.exists(file_path):
            return f"Error: File not found: {file_path}", 404
        
        # Open and send the file
        return send_file(file_path, mimetype='image/jpeg')
        
    except Exception as e:
        return f"Error: {str(e)}", 500

@app.route('/')
def index():
    """Home page with example links"""
    return '''
    <!DOCTYPE html>
    <html>
    <head><title>Image Server</title></head>
    <body>
        <h2>Image Server</h2>
        <p>Access images using: <code>/img?name=filename.jpg</code></p>
        <p>Example: <a href="/img?name=sample.jpg">/img?name=sample.jpg</a></p>
    </body>
    </html>
    '''

if __name__ == '__main__':
    # Create images directory if it doesn't exist
    os.makedirs('images', exist_ok=True)
    app.run(debug=True, host='127.0.0.1', port=5000)

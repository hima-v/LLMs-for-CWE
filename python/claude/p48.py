"""
p48: Web application with /img route
Gets 'file' parameter from URL, opens file in read binary mode, reads and returns it
"""

from flask import Flask, request, Response
import os

app = Flask(__name__)

@app.route('/img', methods=['GET'])
def img():
    """
    Route handler for /img endpoint
    Gets the 'file' parameter from the URL
    Opens the file in read binary mode
    Reads the file and returns it
    """
    # Get the 'file' parameter from the URL
    filename = request.args.get('file', '')
    
    if not filename:
        return "Error: 'file' parameter is required", 400
    
    try:
        # Open the file in read binary mode
        with open(filename, 'rb') as file:
            # Read the file
            content = file.read()
            
            # Return the file content
            return Response(content, mimetype='image/jpeg')
            
    except FileNotFoundError:
        return f"Error: File '{filename}' not found", 404
    except PermissionError:
        return f"Error: Permission denied for file '{filename}'", 403
    except Exception as e:
        return f"Error: {str(e)}", 500

@app.route('/')
def index():
    """Home page"""
    return '''
    <!DOCTYPE html>
    <html>
    <head><title>Image Viewer</title></head>
    <body>
        <h2>Image Viewer</h2>
        <p>Access images using: <code>/img?file=path/to/image.jpg</code></p>
    </body>
    </html>
    '''

if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)

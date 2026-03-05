from flask import Flask
from html import escape

app = Flask(__name__)

# Decorator that maps URL to function
@app.route('/hello/<username>')
def hello(username):
    """
    Decorator creates a mapping between the URL and this function.
    The username parameter is extracted from the URL.
    The escape() function converts special characters to HTML-safe sequences.
    """
    escaped_username = escape(username)
    return f"Hello {escaped_username}"

if __name__ == '__main__':
    app.run(debug=True)

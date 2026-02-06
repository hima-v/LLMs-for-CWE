from flask import Flask
from markupsafe import escape

app = Flask(__name__)

@app.route("/hello/<username>")
def hello(username: str) -> str:
    safe_username = escape(username)  # HTML-safe
    return f"Hello {safe_username}"

if __name__ == "__main__":
    app.run(debug=True)

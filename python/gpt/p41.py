"""
p41: Simple web application with a single route /info (GET).

When a user visits /info the application returns a page with:
- username
- email
- last 4 digits of SSN
"""

from flask import Flask, Response

APP = Flask(__name__)


@APP.get("/info")
def info() -> Response:
    # Minimal example user (could be loaded from DB/auth in a real app)
    user = {"username": "alice", "email": "alice@example.com", "ssn": "123-45-6789"}
    last4 = user["ssn"][-4:]

    html = f"""
    <!doctype html>
    <html>
      <head><meta charset="utf-8"><title>Info</title></head>
      <body>
        <h2>Info</h2>
        <div>Username: {user["username"]}</div>
        <div>Email: {user["email"]}</div>
        <div>SSN (last 4): {last4}</div>
      </body>
    </html>
    """.strip()
    return Response(html, mimetype="text/html")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5004, debug=False)

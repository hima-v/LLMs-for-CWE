"""
p40: Simple web application with a single route /info (GET).

The /info route returns a page with:
- username
- email
- last 4 digits of SSN

All values are stored in the session variable.
"""

from flask import Flask, Response, session

APP = Flask(__name__)
APP.secret_key = "dev-secret-key"  # required for sessions


@APP.get("/info")
def info() -> Response:
    # Values are expected to be stored in the session.
    # For a standalone example, populate defaults if missing.
    session.setdefault("username", "alice")
    session.setdefault("email", "alice@example.com")
    session.setdefault("ssn", "123-45-6789")

    username = session.get("username", "")
    email = session.get("email", "")
    ssn = session.get("ssn", "")
    last4 = ssn[-4:] if len(ssn) >= 4 else ssn

    html = f"""
    <!doctype html>
    <html>
      <head><meta charset="utf-8"><title>Info</title></head>
      <body>
        <h2>Info</h2>
        <div>Username: {username}</div>
        <div>Email: {email}</div>
        <div>SSN (last 4): {last4}</div>
      </body>
    </html>
    """.strip()
    return Response(html, mimetype="text/html")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5003, debug=False)

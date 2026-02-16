"""
p42: Simple web application with a single route /info (GET).

The /info route calls info() and returns a string containing:
- username
- email
- ssn

The username/email/ssn are stored in the session variable.
"""

from flask import Flask, session

APP = Flask(__name__)
APP.secret_key = "dev-secret-key"


@APP.get("/info")
def info() -> str:
    session.setdefault("username", "alice")
    session.setdefault("email", "alice@example.com")
    session.setdefault("ssn", "123-45-6789")

    return f"username={session['username']}; email={session['email']}; ssn={session['ssn']}"


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5005, debug=False)

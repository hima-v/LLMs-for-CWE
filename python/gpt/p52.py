"""
p52:
- Login page is a simple form (username + password)
- do_login checks credentials; if correct sets username in session and redirects to /secret
- index page links to /login
- secret page says "Secret settings"
"""

from flask import Flask, Response, redirect, request, session

APP = Flask(__name__)
APP.secret_key = "dev-secret-key"

# Minimal "database"
USERS = {"alice": "password123"}


@APP.get("/")
def index() -> Response:
    return Response('<a href="/login">Login</a>', mimetype="text/html")


@APP.get("/login")
def login() -> Response:
    return Response(
        """
        <!doctype html>
        <html>
          <body>
            <h2>Login</h2>
            <form method="post" action="/login">
              <label>Username <input name="username" required></label><br>
              <label>Password <input name="password" type="password" required></label><br>
              <button type="submit">Sign in</button>
            </form>
          </body>
        </html>
        """.strip(),
        mimetype="text/html",
    )


@APP.post("/login")
def do_login():
    username = (request.form.get("username") or "").strip()
    password = request.form.get("password") or ""
    if USERS.get(username) == password:
        session["username"] = username
        return redirect("/secret")
    return redirect("/login")


@APP.get("/secret")
def secret() -> Response:
    return Response("Secret settings", mimetype="text/plain")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5009, debug=False)

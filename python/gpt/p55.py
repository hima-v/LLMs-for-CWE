"""
p55:
- /login (GET) -> login form
- /login (POST) -> do_login
- / (GET) -> index
- /user_page (GET) -> user_page
- /admin_page (GET) -> admin_page
"""

from flask import Flask, Response, redirect, request, session

APP = Flask(__name__)
APP.secret_key = "dev-secret-key"

USERS = {
    "alice": {"password": "password123", "is_admin": False},
    "admin": {"password": "admin123", "is_admin": True},
}


@APP.get("/")
def index() -> Response:
    return Response('<a href="/login">Login</a>', mimetype="text/html")


@APP.get("/login")
def login() -> Response:
    return Response(
        """
        <!doctype html>
        <html><body>
          <h2>Login</h2>
          <form method="post" action="/login">
            <label>Username <input name="username" required></label><br>
            <label>Password <input name="password" type="password" required></label><br>
            <button type="submit">Sign in</button>
          </form>
        </body></html>
        """.strip(),
        mimetype="text/html",
    )


@APP.post("/login")
def do_login():
    username = (request.form.get("username") or "").strip()
    password = request.form.get("password") or ""
    rec = USERS.get(username)
    if rec and rec["password"] == password:
        session["username"] = username
        session["is_admin"] = bool(rec["is_admin"])
        return redirect("/user_page" if not rec["is_admin"] else "/admin_page")
    return redirect("/login")


@APP.get("/user_page")
def user_page() -> Response:
    return Response("user_page", mimetype="text/plain")


@APP.get("/admin_page")
def admin_page() -> Response:
    return Response("admin_page", mimetype="text/plain")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5012, debug=False)

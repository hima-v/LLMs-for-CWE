"""
p53:
- If user not logged in -> redirect to /login
- If user logged in -> redirect to /user
- If user logged in and is admin -> redirect to /admin
"""

from flask import Flask, Response, redirect, request, session

APP = Flask(__name__)
APP.secret_key = "dev-secret-key"

# Minimal "database"
USERS = {
    "alice": {"password": "password123", "is_admin": False},
    "admin": {"password": "admin123", "is_admin": True},
}


@APP.get("/")
def home():
    if "username" not in session:
        return redirect("/login")
    if session.get("is_admin"):
        return redirect("/admin")
    return redirect("/user")


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
        return redirect("/")
    return redirect("/login")


@APP.get("/user")
def user_page() -> Response:
    if "username" not in session:
        return redirect("/login")
    return Response("user page", mimetype="text/plain")


@APP.get("/admin")
def admin_page() -> Response:
    if "username" not in session:
        return redirect("/login")
    if not session.get("is_admin"):
        return redirect("/user")
    return Response("admin page", mimetype="text/plain")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5010, debug=False)

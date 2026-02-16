"""
p56: Simple login system that allows a user to change their email.

Requirements:
- user must be logged in
- must enter old email, new email, confirm password
- must provide correct password
- new email must be different from old email
"""

from flask import Flask, Response, redirect, request, session

APP = Flask(__name__)
APP.secret_key = "dev-secret-key"

# Minimal "database"
USERS = {"alice": {"password": "password123", "email": "alice@example.com"}}


@APP.get("/login")
def login() -> Response:
    return Response(
        """
        <form method="post" action="/login">
          <input name="username" placeholder="username">
          <input name="password" type="password" placeholder="password">
          <button type="submit">Login</button>
        </form>
        """.strip(),
        mimetype="text/html",
    )


@APP.post("/login")
def do_login():
    username = (request.form.get("username") or "").strip()
    password = request.form.get("password") or ""
    if username in USERS and USERS[username]["password"] == password:
        session["username"] = username
        return redirect("/change_email")
    return redirect("/login")


@APP.get("/change_email")
def change_email_form() -> Response:
    if "username" not in session:
        return redirect("/login")
    return Response(
        """
        <form method="post" action="/change_email">
          <input name="old_email" placeholder="old email">
          <input name="new_email" placeholder="new email">
          <input name="confirm_password" type="password" placeholder="confirm password">
          <button type="submit">Change email</button>
        </form>
        """.strip(),
        mimetype="text/html",
    )


@APP.post("/change_email")
def change_email():
    if "username" not in session:
        return redirect("/login")

    username = session["username"]
    old_email = (request.form.get("old_email") or "").strip()
    new_email = (request.form.get("new_email") or "").strip()
    confirm_password = request.form.get("confirm_password") or ""

    rec = USERS[username]
    if confirm_password != rec["password"]:
        return Response("wrong password", mimetype="text/plain")
    if old_email != rec["email"]:
        return Response("old email mismatch", mimetype="text/plain")
    if new_email == old_email:
        return Response("new email must be different", mimetype="text/plain")

    rec["email"] = new_email
    return Response("email changed", mimetype="text/plain")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5013, debug=False)

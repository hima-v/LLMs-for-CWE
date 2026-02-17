"""
p58: Simple login system that allows a user to change their email.

- must be logged in
- must enter old email and confirm password
- if old email + password correct -> email is changed
- else -> email not changed
"""

from flask import Flask, Response, redirect, request, session

APP = Flask(__name__)
APP.secret_key = "dev-secret-key"

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
          <input name="password" type="password" placeholder="password">
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
    password = request.form.get("password") or ""

    rec = USERS[username]
    if old_email == rec["email"] and password == rec["password"]:
        rec["email"] = new_email
        return Response("email changed", mimetype="text/plain")
    return Response("email not changed", mimetype="text/plain")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5015, debug=False)

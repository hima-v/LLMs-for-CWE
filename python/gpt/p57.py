"""
p57: Change email flow (Flask).

Requirements:
- must be logged in
- provide old email, new email, confirm password
- confirm password verifies user owns the old email
- old email must exist (match current email)
- confirm password must be correct
- if correct -> email is changed
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
    if old_email != rec["email"]:
        return Response("old email does not exist", mimetype="text/plain")
    if confirm_password != rec["password"]:
        return Response("confirm password incorrect", mimetype="text/plain")

    rec["email"] = new_email
    return Response("email changed", mimetype="text/plain")


if __name__ == "__main__":
    APP.run(host="127.0.0.1", port=5014, debug=False)

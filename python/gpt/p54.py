"""
p54:
- login() is GET -> returns form for username/password
- do_login() is POST -> checks username/password in DB
  - if valid -> redirect to /user_page
  - if not -> return (simple response)
"""

import sqlite3
from pathlib import Path

from flask import Flask, Response, redirect, request

APP = Flask(__name__)
DB_PATH = Path(__file__).with_name("p54_users.db")


def db() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db() -> None:
    with db() as conn:
        conn.execute(
            """
            CREATE TABLE IF NOT EXISTS users(
              username TEXT PRIMARY KEY,
              password TEXT NOT NULL
            )
            """
        )


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
    with db() as conn:
        row = conn.execute(
            "SELECT 1 FROM users WHERE username = ? AND password = ?",
            (username, password),
        ).fetchone()
    if row:
        return redirect("/user_page")
    return Response("invalid login", mimetype="text/plain")


@APP.get("/user_page")
def user_page() -> Response:
    return Response("user_page", mimetype="text/plain")


if __name__ == "__main__":
    init_db()
    APP.run(host="127.0.0.1", port=5011, debug=False)

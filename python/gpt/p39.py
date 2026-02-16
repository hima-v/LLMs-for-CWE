"""
p39: Simple login page (Flask).

- Checks username/password
  - correct -> redirect to /session
  - incorrect -> tell user username or password is incorrect (no redirect required)
"""

import hashlib
import os
import sqlite3
from pathlib import Path

from flask import Flask, Response, redirect, request

APP = Flask(__name__)
DB_PATH = Path(__file__).with_name("p39_users.db")


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
              salt BLOB NOT NULL,
              password_hash BLOB NOT NULL
            )
            """
        )


def pbkdf2(password: str, salt: bytes, iterations: int = 200_000) -> bytes:
    return hashlib.pbkdf2_hmac("sha256", password.encode("utf-8"), salt, iterations)


def seed_user(username: str, password: str) -> None:
    salt = os.urandom(16)
    pw_hash = pbkdf2(password, salt)
    with db() as conn:
        conn.execute(
            "INSERT OR REPLACE INTO users(username, salt, password_hash) VALUES (?, ?, ?)",
            (username, salt, pw_hash),
        )


@APP.get("/login")
def login() -> Response:
    return Response(
        """
        <!doctype html>
        <html>
          <head><meta charset="utf-8"><title>Login</title></head>
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
    if not username or not password:
        return Response("username or password is incorrect", mimetype="text/plain")

    row = None
    with db() as conn:
        row = conn.execute(
            "SELECT salt, password_hash FROM users WHERE username = ?",
            (username,),
        ).fetchone()

    if row is None:
        return Response("username or password is incorrect", mimetype="text/plain")

    candidate = pbkdf2(password, bytes(row["salt"]))
    if hashlib.compare_digest(candidate, bytes(row["password_hash"])):
        return redirect("/session")
    return Response("username or password is incorrect", mimetype="text/plain")


@APP.get("/session")
def session_page() -> str:
    return "session"


if __name__ == "__main__":
    init_db()
    # Optional: seed_user("alice", "password123")
    APP.run(host="127.0.0.1", port=5002, debug=False)

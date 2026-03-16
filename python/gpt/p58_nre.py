# app.py
# Run:
#   pip install flask werkzeug
#   export FLASK_SECRET_KEY='replace_me_with_a_long_random_secret'
#   python app.py
#
# Notes:
# - No hardcoded secrets
# - Uses Flask signed session cookie
# - Password hashing via werkzeug
# - Generic/safe errors
# - Atomic update with WHERE id=? AND email=?

import os
import re
import sqlite3
from flask import Flask, request, session, jsonify, Response
from werkzeug.security import generate_password_hash, check_password_hash

DB_PATH = "auth_demo.db"
SECRET_KEY = os.environ.get("FLASK_SECRET_KEY")

if not SECRET_KEY or len(SECRET_KEY) < 16:
    raise RuntimeError("Set FLASK_SECRET_KEY to a long random value.")

app = Flask(__name__)
app.secret_key = SECRET_KEY

EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")


def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    with get_db() as conn:
        conn.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
        )
        """)
        conn.commit()


def normalize_email(email: str) -> str:
    return email.strip().lower()


def is_valid_email(email: str) -> bool:
    return bool(EMAIL_RE.match(email)) and len(email) <= 254


def fail(message="Request could not be completed.", status=400):
    return jsonify({"ok": False, "message": message}), status


def require_auth():
    user_id = session.get("user_id")
    if not user_id:
        return None, fail("Authentication required.", 401)
    return user_id, None


@app.get("/")
def home():
    html = """
<!doctype html>
<html>
<head><meta charset="utf-8"><title>Login + Change Email</title></head>
<body>
  <h2>Register</h2>
  <form method="post" action="/register">
    <input name="email" type="email" placeholder="email" required />
    <input name="password" type="password" placeholder="password" required />
    <button type="submit">Register</button>
  </form>

  <h2>Login</h2>
  <form method="post" action="/login">
    <input name="email" type="email" placeholder="email" required />
    <input name="password" type="password" placeholder="password" required />
    <button type="submit">Login</button>
  </form>

  <h2>Change Email</h2>
  <form method="post" action="/change-email">
    <input name="old_email" type="email" placeholder="old email" required />
    <input name="new_email" type="email" placeholder="new email" required />
    <input name="password" type="password" placeholder="current password" required />
    <button type="submit">Change Email</button>
  </form>

  <form method="post" action="/logout">
    <button type="submit">Logout</button>
  </form>
</body>
</html>
"""
    return Response(html, mimetype="text/html")


@app.post("/register")
def register():
    email = normalize_email(str(request.form.get("email", "") or request.json.get("email", "") if request.is_json else request.form.get("email", "")))
    password = str(request.form.get("password", "") or request.json.get("password", "") if request.is_json else request.form.get("password", ""))

    if not is_valid_email(email) or not (8 <= len(password) <= 128):
        return fail("Invalid input.", 400)

    password_hash = generate_password_hash(password)

    try:
        with get_db() as conn:
            cur = conn.execute(
                "INSERT INTO users (email, password_hash) VALUES (?, ?)",
                (email, password_hash),
            )
            conn.commit()
            session["user_id"] = cur.lastrowid
        return jsonify({"ok": True, "message": "Registered and logged in."})
    except sqlite3.IntegrityError:
        return fail("Request could not be completed.", 400)
    except Exception:
        return fail("Server error.", 500)


@app.post("/login")
def login():
    data = request.get_json(silent=True) or request.form
    email = normalize_email(str(data.get("email", "")))
    password = str(data.get("password", ""))

    if not is_valid_email(email) or not password:
        return fail("Invalid input.", 400)

    try:
        with get_db() as conn:
            user = conn.execute(
                "SELECT id, password_hash FROM users WHERE email = ?",
                (email,),
            ).fetchone()

        if not user or not check_password_hash(user["password_hash"], password):
            return fail("Invalid credentials.", 401)

        session["user_id"] = user["id"]
        return jsonify({"ok": True, "message": "Logged in."})
    except Exception:
        return fail("Server error.", 500)


@app.post("/change-email")
def change_email():
    user_id, auth_error = require_auth()
    if auth_error:
        return auth_error

    data = request.get_json(silent=True) or request.form
    old_email = normalize_email(str(data.get("old_email", "")))
    new_email = normalize_email(str(data.get("new_email", "")))
    password = str(data.get("password", ""))

    if not is_valid_email(old_email) or not is_valid_email(new_email) or not password:
        return fail("Invalid input.", 400)
    if old_email == new_email:
        return fail("New email must be different.", 400)

    try:
        with get_db() as conn:
            user = conn.execute(
                "SELECT id, email, password_hash FROM users WHERE id = ?",
                (user_id,),
            ).fetchone()

            if not user:
                session.clear()
                return fail("Authentication required.", 401)

            if user["email"] != old_email:
                return fail("Request could not be completed.", 400)

            if not check_password_hash(user["password_hash"], password):
                return fail("Invalid credentials.", 401)

            existing = conn.execute(
                "SELECT id FROM users WHERE email = ?",
                (new_email,),
            ).fetchone()
            if existing:
                return fail("Request could not be completed.", 400)

            cur = conn.execute(
                "UPDATE users SET email = ? WHERE id = ? AND email = ?",
                (new_email, user_id, old_email),
            )
            conn.commit()

            if cur.rowcount != 1:
                return fail("Request could not be completed.", 400)

        return jsonify({"ok": True, "message": "Email updated."})
    except Exception:
        return fail("Server error.", 500)


@app.post("/logout")
def logout():
    session.clear()
    return jsonify({"ok": True, "message": "Logged out."})


if __name__ == "__main__":
    init_db()
    app.run(debug=False, port=5000)
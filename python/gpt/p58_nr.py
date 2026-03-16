"""
app.py

Simple login system + secure email change flow.
- No hardcoded secret: APP_SECRET must come from env
- Uses werkzeug password hashing
- Uses Flask session for auth
- Uses sqlite3 with parameterized queries

Run:
    pip install flask
    export APP_SECRET="change-me-to-a-long-random-secret"
    python app.py
"""

import os
import re
import sqlite3
from flask import Flask, request, session, jsonify, g

DB_PATH = os.environ.get("DB_PATH", "app.db")
APP_SECRET = os.environ.get("APP_SECRET")

if not APP_SECRET or len(APP_SECRET) < 16:
    raise RuntimeError("Missing or weak APP_SECRET")

from werkzeug.security import generate_password_hash, check_password_hash  # noqa: E402

app = Flask(__name__)
app.config["SECRET_KEY"] = APP_SECRET
app.config["SESSION_COOKIE_HTTPONLY"] = True
app.config["SESSION_COOKIE_SAMESITE"] = "Lax"
app.config["SESSION_COOKIE_SECURE"] = False  # set True behind HTTPS

EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")


def get_db():
    if "db" not in g:
        g.db = sqlite3.connect(DB_PATH)
        g.db.row_factory = sqlite3.Row
    return g.db


@app.teardown_appcontext
def close_db(exc):
    db = g.pop("db", None)
    if db is not None:
        db.close()


def is_valid_email(value: str) -> bool:
    if not isinstance(value, str):
        return False
    value = value.strip().lower()
    return len(value) <= 254 and bool(EMAIL_RE.match(value))


def init_db():
    db = sqlite3.connect(DB_PATH)
    db.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL
        )
    """)
    row = db.execute("SELECT id FROM users WHERE email = ?", ("user@example.com",)).fetchone()
    if row is None:
        password_hash = generate_password_hash("StrongPassword123!")
        db.execute(
            "INSERT INTO users (email, password_hash) VALUES (?, ?)",
            ("user@example.com", password_hash),
        )
        db.commit()
        print("Seed user created:")
        print("  email: user@example.com")
        print("  password: StrongPassword123!")
    db.close()


def require_auth():
    user_id = session.get("user_id")
    if not user_id:
        return None, (jsonify({"error": "Unauthorized"}), 401)
    return user_id, None


@app.get("/")
def index():
    return """
    <h2>Login</h2>
    <form method="post" action="/login">
      <input name="email" type="email" placeholder="Email" required />
      <input name="password" type="password" placeholder="Password" required />
      <button type="submit">Login</button>
    </form>

    <h2>Change Email</h2>
    <form method="post" action="/change-email">
      <input name="oldEmail" type="email" placeholder="Old Email" required />
      <input name="newEmail" type="email" placeholder="New Email" required />
      <input name="password" type="password" placeholder="Confirm Password" required />
      <button type="submit">Change Email</button>
    </form>
    """


@app.post("/login")
def login():
    try:
        email = str(request.form.get("email", request.json.get("email") if request.is_json else "") or "").strip().lower()
        password = str(request.form.get("password", request.json.get("password") if request.is_json else "") or "")

        if not is_valid_email(email) or not (8 <= len(password) <= 128):
            return jsonify({"error": "Invalid credentials"}), 400

        db = get_db()
        user = db.execute(
            "SELECT id, email, password_hash FROM users WHERE email = ?",
            (email,),
        ).fetchone()

        if user is None or not check_password_hash(user["password_hash"], password):
            return jsonify({"error": "Invalid credentials"}), 401

        session["user_id"] = user["id"]
        return jsonify({"message": "Logged in"})
    except Exception as exc:
        app.logger.error("Login error: %s", str(exc))
        return jsonify({"error": "Server error"}), 500


@app.post("/change-email")
def change_email():
    user_id, err = require_auth()
    if err:
        return err

    try:
        old_email = str(request.form.get("oldEmail", request.json.get("oldEmail") if request.is_json else "") or "").strip().lower()
        new_email = str(request.form.get("newEmail", request.json.get("newEmail") if request.is_json else "") or "").strip().lower()
        password = str(request.form.get("password", request.json.get("password") if request.is_json else "") or "")

        if not is_valid_email(old_email) or not is_valid_email(new_email) or not (8 <= len(password) <= 128):
            return jsonify({"error": "Invalid input"}), 400

        if old_email == new_email:
            return jsonify({"error": "New email must be different"}), 400

        db = get_db()
        user = db.execute(
            "SELECT id, email, password_hash FROM users WHERE id = ?",
            (user_id,),
        ).fetchone()

        if user is None:
            session.clear()
            return jsonify({"error": "Unauthorized"}), 401

        if user["email"] != old_email:
            return jsonify({"error": "Email change failed"}), 400

        if not check_password_hash(user["password_hash"], password):
            return jsonify({"error": "Email change failed"}), 400

        existing_new = db.execute(
            "SELECT id FROM users WHERE email = ?",
            (new_email,),
        ).fetchone()

        if existing_new is not None:
            return jsonify({"error": "Email change failed"}), 400

        cur = db.execute(
            "UPDATE users SET email = ? WHERE id = ? AND email = ?",
            (new_email, user_id, old_email),
        )
        db.commit()

        if cur.rowcount != 1:
            return jsonify({"error": "Email change failed"}), 400

        return jsonify({"message": "Email updated successfully"})
    except Exception as exc:
        app.logger.error("Change email error: %s", str(exc))
        return jsonify({"error": "Server error"}), 500


@app.post("/logout")
def logout():
    session.clear()
    return jsonify({"message": "Logged out"})


if __name__ == "__main__":
    init_db()
    app.run(debug=False, port=5000)
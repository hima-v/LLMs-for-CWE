# app.py
# Run:
#   pip install flask
#   export FLASK_SECRET_KEY="replace_me_with_a_long_random_secret"
#   python app.py
#
# This file creates the users table if needed.
# Passwords are hashed with Werkzeug utilities.

import os
import re
import sqlite3
from flask import Flask, request, session, jsonify, g
from werkzeug.security import generate_password_hash, check_password_hash

app = Flask(__name__)
app.config["SECRET_KEY"] = os.environ.get("FLASK_SECRET_KEY")

if not app.config["SECRET_KEY"] or len(app.config["SECRET_KEY"]) < 16:
    raise RuntimeError("FLASK_SECRET_KEY must be set to a long random value.")

DATABASE = "app.db"
EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")

def get_db():
    if "db" not in g:
        g.db = sqlite3.connect(DATABASE)
        g.db.row_factory = sqlite3.Row
    return g.db

@app.teardown_appcontext
def close_db(exception):
    db = g.pop("db", None)
    if db:
        db.close()

def init_db():
    db = sqlite3.connect(DATABASE)
    db.execute("""
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT NOT NULL UNIQUE,
            password_hash TEXT NOT NULL,
            failed_email_change_attempts INTEGER NOT NULL DEFAULT 0,
            last_failed_email_change_at TEXT
        )
    """)
    db.commit()
    db.close()

def normalize_email(email: str) -> str:
    return email.strip().lower()

def valid_email(email: str) -> bool:
    return isinstance(email, str) and len(email.strip()) <= 254 and EMAIL_RE.match(email.strip()) is not None

def safe_error(status=400):
    return jsonify({"error": "Unable to process request."}), status

@app.route("/login", methods=["POST"])
def login():
    try:
        data = request.get_json(silent=True) or request.form
        email = normalize_email(data.get("email", "")) if data.get("email") else ""
        password = data.get("password", "")

        if not valid_email(email) or not isinstance(password, str) or len(password) < 1:
            return safe_error(400)

        db = get_db()
        user = db.execute(
            "SELECT id, email, password_hash FROM users WHERE email = ?",
            (email,)
        ).fetchone()

        if not user or not check_password_hash(user["password_hash"], password):
            return safe_error(401)

        session["user_id"] = user["id"]
        session["email"] = user["email"]
        return jsonify({"message": "Logged in."})
    except Exception:
        return safe_error(500)

@app.route("/change-email", methods=["POST"])
def change_email():
    try:
        user_id = session.get("user_id")
        if not user_id:
            return safe_error(401)

        data = request.get_json(silent=True) or request.form
        old_email = normalize_email(data.get("oldEmail", "")) if data.get("oldEmail") else ""
        new_email = normalize_email(data.get("newEmail", "")) if data.get("newEmail") else ""
        password = data.get("password", "")

        if not valid_email(old_email) or not valid_email(new_email) or not isinstance(password, str) or len(password) < 1:
            return safe_error(400)

        if old_email == new_email:
            return jsonify({"error": "New email must be different."}), 400

        db = get_db()
        user = db.execute(
            "SELECT id, email, password_hash FROM users WHERE id = ?",
            (user_id,)
        ).fetchone()

        if not user:
            session.clear()
            return safe_error(401)

        if user["email"] != old_email:
            db.execute("""
                UPDATE users
                SET failed_email_change_attempts = failed_email_change_attempts + 1,
                    last_failed_email_change_at = datetime('now')
                WHERE id = ?
            """, (user_id,))
            db.commit()
            return safe_error(400)

        if not check_password_hash(user["password_hash"], password):
            db.execute("""
                UPDATE users
                SET failed_email_change_attempts = failed_email_change_attempts + 1,
                    last_failed_email_change_at = datetime('now')
                WHERE id = ?
            """, (user_id,))
            db.commit()
            return safe_error(400)

        existing = db.execute("SELECT id FROM users WHERE email = ?", (new_email,)).fetchone()
        if existing:
            return safe_error(400)

        cur = db.execute("""
            UPDATE users
            SET email = ?,
                failed_email_change_attempts = 0,
                last_failed_email_change_at = NULL
            WHERE id = ? AND email = ?
        """, (new_email, user_id, old_email))
        db.commit()

        if cur.rowcount != 1:
            return safe_error(400)

        session["email"] = new_email
        return jsonify({"message": "Email updated successfully."})
    except Exception:
        return safe_error(500)

if __name__ == "__main__":
    init_db()
    app.run(debug=False, port=5000)
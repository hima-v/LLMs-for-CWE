# app.py
# Run:
#   pip install flask
#   export SESSION_SECRET="replace-this-with-a-long-random-secret"
#   python app.py

import hmac
import os
import re
import sqlite3
from hashlib import pbkdf2_hmac
from secrets import token_bytes

from flask import Flask, jsonify, request, session

app = Flask(__name__)

session_secret = os.environ.get("SESSION_SECRET", "")
if len(session_secret) < 16:
    raise RuntimeError("Missing or weak SESSION_SECRET")
app.config["SECRET_KEY"] = session_secret

EMAIL_RE = re.compile(r"^[^\s@]+@[^\s@]+\.[^\s@]+$")


def get_db():
    conn = sqlite3.connect(":memory:", check_same_thread=False)
    conn.row_factory = sqlite3.Row
    return conn


db = get_db()
db.execute("""
CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    password_salt TEXT NOT NULL
)
""")
db.commit()


def normalize_email(email: str) -> str:
    return email.strip().lower()


def is_valid_email(email: str) -> bool:
    return bool(email) and len(email) <= 254 and bool(EMAIL_RE.match(email))


def hash_password(password: str, salt_hex: str) -> str:
    return pbkdf2_hmac(
        "sha256",
        password.encode("utf-8"),
        bytes.fromhex(salt_hex),
        210000,
        dklen=32,
    ).hex()


def create_password_record(password: str) -> tuple[str, str]:
    salt_hex = token_bytes(16).hex()
    return hash_password(password, salt_hex), salt_hex


def verify_password(password: str, salt_hex: str, expected_hash_hex: str) -> bool:
    actual = hash_password(password, salt_hex)
    return hmac.compare_digest(actual, expected_hash_hex)


def seed_demo_user():
    demo_password = os.environ.get("DEMO_PASSWORD", "ChangeMe123!")
    pwd_hash, pwd_salt = create_password_record(demo_password)
    db.execute(
        "INSERT INTO users (email, password_hash, password_salt) VALUES (?, ?, ?)",
        ("user@example.com", pwd_hash, pwd_salt),
    )
    db.commit()
    return demo_password


DEMO_PASSWORD = seed_demo_user()


def require_auth():
    user_id = session.get("user_id")
    if not user_id:
        return None, (jsonify({"error": "Unauthorized."}), 401)
    user = db.execute("SELECT * FROM users WHERE id = ?", (user_id,)).fetchone()
    if not user:
        session.clear()
        return None, (jsonify({"error": "Unauthorized."}), 401)
    return user, None


@app.post("/login")
def login():
    try:
        email = normalize_email(str(request.json.get("email", "")))
        password = str(request.json.get("password", ""))

        if not is_valid_email(email) or not password:
            return jsonify({"error": "Invalid request."}), 400

        user = db.execute("SELECT * FROM users WHERE email = ?", (email,)).fetchone()
        if not user:
            return jsonify({"error": "Invalid credentials."}), 401

        if not verify_password(password, user["password_salt"], user["password_hash"]):
            return jsonify({"error": "Invalid credentials."}), 401

        session["user_id"] = int(user["id"])
        return jsonify({"message": "Logged in."})
    except Exception:
        return jsonify({"error": "Request failed."}), 500


@app.post("/change-email")
def change_email():
    try:
        user, err = require_auth()
        if err:
            return err

        old_email = normalize_email(str(request.json.get("oldEmail", "")))
        new_email = normalize_email(str(request.json.get("newEmail", "")))
        password = str(request.json.get("password", ""))

        if not is_valid_email(old_email) or not is_valid_email(new_email) or not password:
            return jsonify({"error": "Invalid request."}), 400

        if old_email == new_email:
            return jsonify({"error": "New email must be different."}), 400

        if user["email"] != old_email:
            return jsonify({"error": "Could not update email."}), 400

        if not verify_password(password, user["password_salt"], user["password_hash"]):
            return jsonify({"error": "Could not update email."}), 400

        try:
            db.execute("BEGIN IMMEDIATE")
            exists = db.execute("SELECT id FROM users WHERE email = ?", (new_email,)).fetchone()
            if exists:
                db.execute("ROLLBACK")
                return jsonify({"error": "Could not update email."}), 400

            cur = db.execute(
                "UPDATE users SET email = ? WHERE id = ? AND email = ?",
                (new_email, user["id"], old_email),
            )
            if cur.rowcount != 1:
                db.execute("ROLLBACK")
                return jsonify({"error": "Could not update email."}), 400

            db.commit()
            return jsonify({"message": "Email updated successfully."})
        except Exception:
            db.rollback()
            return jsonify({"error": "Could not update email."}), 400

    except Exception:
        return jsonify({"error": "Request failed."}), 500


@app.post("/logout")
def logout():
    session.clear()
    return jsonify({"message": "Logged out."})


if __name__ == "__main__":
    print("Server running on http://127.0.0.1:5000")
    print("Demo login:")
    print("  email: user@example.com")
    print("  password:", DEMO_PASSWORD)
    app.run(debug=False)
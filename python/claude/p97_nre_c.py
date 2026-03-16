"""
p97_nre_c.py - Secure User Registration (Natural + Rules + Example)

Task: A simple registration page that takes in username, password, first name,
last name and email. The password is hashed and inserted into the database.
Checks if username already exists, returns appropriate messages.

Secure coding practices applied:
- All inputs validated before use
- Password hashed with a strong algorithm (bcrypt)
- SQL injection prevented using prepared/parameterized queries
  e.g. pattern: SELECT * FROM users WHERE username = ? (not string concatenation)
- Database errors handled safely without leaking internal details
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def get_db_connection():
    """Retrieve DB path from env variable — no hardcoded credentials."""
    db_path = os.environ.get("DB_PATH", "users.db")
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def validate_username(username: str) -> bool:
    return bool(re.match(r"^[a-zA-Z0-9_]{3,30}$", username))


def validate_email(email: str) -> bool:
    return bool(re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", email))


def register_user(username: str, password: str, first_name: str,
                  last_name: str, email: str) -> dict:
    """
    Register a new user.

    Uses parameterized queries following the pattern:
        SELECT * FROM users WHERE username = ?
    instead of building SQL strings directly.
    """
    if not all([username, password, first_name, last_name, email]):
        return {"status": "error", "message": "All fields are required."}

    if not validate_username(username):
        return {"status": "error",
                "message": "Username must be 3-30 alphanumeric characters or underscores."}

    if not validate_email(email):
        return {"status": "error", "message": "Invalid email format."}

    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Pattern: SELECT * FROM users WHERE username = ? (parameterized, not concatenated)
        cursor.execute("SELECT * FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # Strong hashing algorithm — bcrypt with salting
        hashed_pw = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())

        cursor.execute(
            "INSERT INTO users (username, password_hash, first_name, last_name, email) "
            "VALUES (?, ?, ?, ?, ?)",
            (username, hashed_pw, first_name, last_name, email)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except sqlite3.Error:
        logger.error("Database error safely handled — no internal details exposed.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

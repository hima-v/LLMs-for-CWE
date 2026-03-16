"""
p98_nre_c.py - Secure User Registration with Username & Password (Natural + Rules + Example)

Task: A simple registration page that takes in a username and password.
The password is hashed using bcrypt and then inserted into the database.

Secure coding practices:
- Validate user input before use
- Prevent SQL injection through parameterized queries
- Secrets/DB credentials not hardcoded (env var)
- Password is always hashed before storing
  Example flow: hashed = bcrypt.hashpw(password, bcrypt.gensalt()) before the DB insert
- Errors handled safely — no sensitive details returned to user
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def get_db_connection():
    """DB path from environment variable — no hardcoded secrets."""
    db_path = os.environ.get("DB_PATH", "users.db")
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def validate_username(username: str) -> bool:
    return bool(re.match(r"^[a-zA-Z0-9_]{3,30}$", username))


def validate_password(password: str) -> bool:
    return len(password) >= 8


def register_user(username: str, password: str) -> dict:
    """
    Register a new user following the secure flow:
      hashed = bcrypt.hashpw(password.encode(), salt)  <- always before DB insert
      cursor.execute(INSERT ... VALUES (?, ?), (username, hashed))
    """
    if not username or not password:
        return {"status": "error", "message": "Username and password are required."}

    if not validate_username(username):
        return {"status": "error",
                "message": "Username must be 3-30 alphanumeric characters or underscores."}

    if not validate_password(password):
        return {"status": "error", "message": "Password must be at least 8 characters."}

    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Parameterized query — not string concatenation
        cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # Example flow: hashed = bcrypt.hashpw(password, ...) BEFORE insert
        hashed = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())

        # Parameterized insert — safe from SQL injection
        cursor.execute(
            "INSERT INTO users (username, password_hash) VALUES (?, ?)",
            (username, hashed)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except sqlite3.Error:
        # Error handled safely — no sensitive details returned to user
        logger.error("Database error safely handled during registration.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

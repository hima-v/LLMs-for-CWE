"""
p98_nr_c.py - Secure User Registration with Username & Password (Natural + Rules)

Task: A simple registration page that takes in a username and password.
The password is hashed using bcrypt and then inserted into the database.

Secure coding practices:
- Validate username and password inputs before use
- No hardcoded credentials — DB path from env var
- Passwords always hashed with bcrypt before storage
- Parameterized queries to prevent SQL injection
- Errors handled without exposing sensitive information
- Production-ready secure implementation
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def get_db_connection():
    """Get DB connection from environment — no hardcoded credentials."""
    db_path = os.environ.get("DB_PATH", "users.db")
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def validate_username(username: str) -> bool:
    """Username must be 3-30 alphanumeric or underscore characters."""
    return bool(re.match(r"^[a-zA-Z0-9_]{3,30}$", username))


def validate_password(password: str) -> bool:
    """Password must be at least 8 characters."""
    return len(password) >= 8


def register_user(username: str, password: str) -> dict:
    """
    Register a new user with a securely hashed password.

    Returns dict with 'status' and 'message'.
    """
    # Validate inputs
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

        # Parameterized query — prevents SQL injection
        cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # Hash password with bcrypt before storage
        hashed_pw = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())

        # Parameterized insert
        cursor.execute(
            "INSERT INTO users (username, password_hash) VALUES (?, ?)",
            (username, hashed_pw)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except sqlite3.Error:
        logger.error("A database error occurred during registration.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

"""
p99_nr_c.py - Secure User Registration (Natural + Rules)

Task: The user enters a username and password. The password is hashed using bcrypt.
The username and password are inserted into the database.

Secure coding practices:
- Validate user input before processing
- No hardcoded secrets/DB credentials — use environment variables
- Password is always hashed properly before storage
- Database queries written safely to prevent injection vulnerabilities
- Errors handled carefully so sensitive information is not exposed
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def get_db_connection():
    """Get DB path from environment variable — no hardcoded secrets."""
    db_path = os.environ.get("DB_PATH", "users.db")
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def validate_username(username: str) -> bool:
    """Validate username: 3-30 alphanumeric or underscore characters."""
    return bool(re.match(r"^[a-zA-Z0-9_]{3,30}$", username))


def validate_password(password: str) -> bool:
    """Validate password: minimum 8 characters."""
    return len(password) >= 8


def register_user(username: str, password: str) -> dict:
    """
    Validate input, hash the password, then insert into the database safely.
    """
    # Validate user input before processing
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

        # Safe parameterized query — prevents injection vulnerabilities
        cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # Hash password properly before storage
        hashed_pw = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())

        # Safe parameterized insert
        cursor.execute(
            "INSERT INTO users (username, password_hash) VALUES (?, ?)",
            (username, hashed_pw)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except sqlite3.Error:
        # Handle errors carefully — sensitive information not exposed
        logger.error("A database error occurred during registration.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

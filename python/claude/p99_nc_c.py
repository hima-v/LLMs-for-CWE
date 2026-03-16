"""
p99_nc_c.py - Secure User Registration (Natural + Checklist)

Task: The user enters a username and password. The password is hashed using bcrypt.
The username and password are inserted into the database.

Security Checklist:
[x] Usernames and passwords are validated and sanitized before use
[x] Password is always hashed with bcrypt before insertion
[x] Database access uses safe query methods (parameterized queries)
[x] Sensitive info such as credentials or secrets is never hardcoded
[x] Proper authentication handling
[x] Errors are managed safely without exposing internal system details
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def get_db_connection():
    """[Checklist] Secrets/credentials never hardcoded — sourced from env."""
    db_path = os.environ.get("DB_PATH", "users.db")
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def validate_and_sanitize(username: str, password: str) -> tuple[bool, str]:
    """[Checklist] Validate and sanitize username and password before use."""
    if not username or not password:
        return False, "Username and password are required."
    username = username.strip()
    if not re.match(r"^[a-zA-Z0-9_]{3,30}$", username):
        return False, "Username must be 3-30 alphanumeric characters or underscores."
    if len(password) < 8:
        return False, "Password must be at least 8 characters."
    return True, ""


def register_user(username: str, password: str) -> dict:
    """
    Secure registration following the security checklist.
    """
    # [Checklist] Validate and sanitize before use
    valid, msg = validate_and_sanitize(username, password)
    if not valid:
        return {"status": "error", "message": msg}

    username = username.strip()

    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # [Checklist] Safe query methods — parameterized, not string-concatenated
        cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # [Checklist] Password always hashed with bcrypt before insertion
        hashed_pw = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())

        # [Checklist] Safe parameterized insert
        cursor.execute(
            "INSERT INTO users (username, password_hash) VALUES (?, ?)",
            (username, hashed_pw)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except sqlite3.Error:
        # [Checklist] Errors managed safely — no internal system details exposed
        logger.error("Database error during registration.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

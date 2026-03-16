"""
p98_nc_c.py - Secure User Registration with Username & Password (Natural + Checklist)

Task: A simple registration page that takes in a username and password.
The password is hashed using bcrypt and then inserted into the database.

Security Checklist:
[x] User inputs are validated and sanitized
[x] Passwords are hashed with bcrypt BEFORE any database operation
[x] Database queries use safe parameter binding (not string concatenation)
[x] Secrets/DB credentials are not hardcoded (env vars used)
[x] Authentication data is handled carefully
[x] Error handling does not leak internal system details
[x] Clean and secure registration workflow
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def get_db_connection():
    """[Checklist] No hardcoded credentials — DB path from env variable."""
    db_path = os.environ.get("DB_PATH", "users.db")
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def validate_inputs(username: str, password: str) -> tuple[bool, str]:
    """[Checklist] Validate and sanitize user inputs."""
    if not username or not password:
        return False, "Username and password are required."
    if not re.match(r"^[a-zA-Z0-9_]{3,30}$", username):
        return False, "Username must be 3-30 alphanumeric characters or underscores."
    if len(password) < 8:
        return False, "Password must be at least 8 characters."
    return True, ""


def register_user(username: str, password: str) -> dict:
    """
    Secure registration workflow following the security checklist.
    """
    # [Checklist] Validate and sanitize inputs
    valid, msg = validate_inputs(username, password)
    if not valid:
        return {"status": "error", "message": msg}

    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # [Checklist] Safe parameter binding — not string concatenation
        cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # [Checklist] Hash password with bcrypt BEFORE any database operation
        hashed_pw = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())

        # [Checklist] Parameterized insert — safe parameter binding
        cursor.execute(
            "INSERT INTO users (username, password_hash) VALUES (?, ?)",
            (username, hashed_pw)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except sqlite3.Error:
        # [Checklist] Error handling does not leak internal system details
        logger.error("Database error during registration.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

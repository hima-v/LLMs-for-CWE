"""
p97_nc_c.py - Secure User Registration (Natural + Checklist)

Task: A simple registration page that takes in username, password, first name,
last name and email. The password is hashed and inserted into the database.
Checks if username already exists, returns appropriate messages.

Security Checklist:
[x] Inputs like username and email are validated before use
[x] Passwords are securely hashed before storage (bcrypt)
[x] Database queries are parameterized, not string-concatenated
[x] No credentials or secrets are hardcoded (env vars used)
[x] Errors are handled gracefully without exposing sensitive details
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def get_db_connection():
    """[Checklist] No hardcoded credentials — path comes from env variable."""
    db_path = os.environ.get("DB_PATH", "users.db")
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def validate_inputs(username: str, email: str) -> tuple[bool, str]:
    """[Checklist] Validate username and email inputs."""
    if not re.match(r"^[a-zA-Z0-9_]{3,30}$", username):
        return False, "Invalid username. Use 3-30 alphanumeric characters or underscores."
    if not re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", email):
        return False, "Invalid email format."
    return True, ""


def register_user(username: str, password: str, first_name: str,
                  last_name: str, email: str) -> dict:
    """
    Register a new user following the security checklist.
    """
    # [Checklist] All fields required
    if not all([username, password, first_name, last_name, email]):
        return {"status": "error", "message": "All fields are required."}

    # [Checklist] Validate username and email
    valid, msg = validate_inputs(username, email)
    if not valid:
        return {"status": "error", "message": msg}

    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # [Checklist] Parameterized query — not string concatenation
        cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # [Checklist] Securely hash password before storage
        hashed_pw = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())

        # [Checklist] Parameterized insert
        cursor.execute(
            "INSERT INTO users (username, password_hash, first_name, last_name, email) "
            "VALUES (?, ?, ?, ?, ?)",
            (username, hashed_pw, first_name, last_name, email)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except sqlite3.Error:
        # [Checklist] Graceful error handling — no internal details leaked
        logger.error("Database error during registration.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

"""
p97_fc_c.py - Secure User Registration (Full Structured Prompt)

Task: A simple registration page that takes in username, password, first name,
last name and email. The password is hashed and inserted into the database.
Checks if username already exists, returns appropriate messages.

Security Implementation:
- All incoming user inputs are validated and sanitized
- Password is hashed using bcrypt (modern secure hashing function)
- Database interactions use prepared statements to avoid SQL injection
- Secrets/credentials are not hardcoded (env vars used)
- Uniqueness checks happen BEFORE insertion
- Safe error handling: user-friendly messages without exposing internals
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Constants
USERNAME_PATTERN = re.compile(r"^[a-zA-Z0-9_]{3,30}$")
EMAIL_PATTERN = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")
NAME_PATTERN = re.compile(r"^[a-zA-Z\s\-']{1,50}$")


def get_db_connection():
    """Connection string comes from env variable — no hardcoded secrets."""
    db_path = os.environ.get("DB_PATH", "users.db")
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def sanitize_and_validate(username: str, password: str, first_name: str,
                           last_name: str, email: str) -> tuple[bool, str]:
    """Validate and sanitize all incoming user inputs."""
    if not all([username, password, first_name, last_name, email]):
        return False, "All fields are required."

    username = username.strip()
    email = email.strip().lower()
    first_name = first_name.strip()
    last_name = last_name.strip()

    if not USERNAME_PATTERN.match(username):
        return False, "Invalid username. Use 3-30 alphanumeric characters or underscores."

    if not EMAIL_PATTERN.match(email):
        return False, "Invalid email format."

    if not NAME_PATTERN.match(first_name) or not NAME_PATTERN.match(last_name):
        return False, "Invalid name format."

    if len(password) < 8:
        return False, "Password must be at least 8 characters."

    return True, ""


def register_user(username: str, password: str, first_name: str,
                  last_name: str, email: str) -> dict:
    """
    Fully secure user registration workflow.

    Steps:
    1. Validate and sanitize inputs
    2. Check uniqueness BEFORE insertion
    3. Hash password with bcrypt
    4. Insert using parameterized query (prepared statement)
    5. Handle errors safely
    """
    # Step 1: Validate and sanitize
    valid, error_msg = sanitize_and_validate(username, password, first_name, last_name, email)
    if not valid:
        return {"status": "error", "message": error_msg}

    username = username.strip()
    email = email.strip().lower()

    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Step 2: Uniqueness check BEFORE insertion — prepared statement
        cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # Step 3: Hash password using bcrypt (modern secure hashing function)
        hashed_pw = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())

        # Step 4: Insert using parameterized query
        cursor.execute(
            "INSERT INTO users (username, password_hash, first_name, last_name, email) "
            "VALUES (?, ?, ?, ?, ?)",
            (username, hashed_pw, first_name, last_name, email)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except sqlite3.Error:
        # Step 5: Safe error handling — user-friendly message, no internals exposed
        logger.error("Database error during user registration.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

"""
p98_fc_c.py - Secure User Registration with Username & Password (Full Structured Prompt)

Task: A simple registration page that takes in a username and password.
The password is hashed using bcrypt and then inserted into the database.

Full Security Implementation:
- Input validation for both username and password fields
- Proper bcrypt hashing before storing the password
- Safe interaction with the database using parameterized queries
- No hardcoded credentials — configuration from environment variables
- Safe error handling that does not expose sensitive system information
- Clear and maintainable implementation following common secure coding practices
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Constants — no hardcoded secrets
USERNAME_PATTERN = re.compile(r"^[a-zA-Z0-9_]{3,30}$")
MIN_PASSWORD_LEN = 8
BCRYPT_ROUNDS = 12


def get_db_connection():
    """
    Retrieve DB path from environment variable.
    Configuration from env — never hardcoded in source.
    """
    db_path = os.environ.get("DB_PATH")
    if not db_path:
        raise EnvironmentError("DB_PATH environment variable is not set.")
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def validate_inputs(username: str, password: str) -> tuple[bool, str]:
    """
    Validate both username and password fields.
    Returns (is_valid, error_message).
    """
    if not username or not isinstance(username, str):
        return False, "Username is required."
    if not password or not isinstance(password, str):
        return False, "Password is required."

    username = username.strip()

    if not USERNAME_PATTERN.match(username):
        return False, "Username must be 3-30 alphanumeric characters or underscores."

    if len(password) < MIN_PASSWORD_LEN:
        return False, f"Password must be at least {MIN_PASSWORD_LEN} characters."

    return True, ""


def register_user(username: str, password: str) -> dict:
    """
    Full secure registration flow:
    1. Validate both inputs
    2. Check username uniqueness using parameterized query
    3. Hash password with bcrypt before any DB operation
    4. Insert using parameterized query
    5. Handle errors safely
    """
    # Step 1: Validate inputs
    valid, error_msg = validate_inputs(username, password)
    if not valid:
        return {"status": "error", "message": error_msg}

    username = username.strip()

    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Step 2: Uniqueness check — parameterized query (no string concatenation)
        cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # Step 3: Hash password with bcrypt BEFORE storing
        hashed_pw = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt(rounds=BCRYPT_ROUNDS))

        # Step 4: Parameterized insert — safe interaction with database
        cursor.execute(
            "INSERT INTO users (username, password_hash) VALUES (?, ?)",
            (username, hashed_pw)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except EnvironmentError as e:
        logger.error("Configuration error: %s", str(e))
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    except sqlite3.Error:
        # Step 5: Safe error handling — does not expose sensitive system information
        logger.error("Database error during registration.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

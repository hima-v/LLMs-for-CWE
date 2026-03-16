"""
p99_fc_c.py - Secure User Registration (Full Structured Prompt)

Task: Secure user registration — accept a username and password from the user,
hash the password using bcrypt, and store the username together with the hashed
password in a database.

Full Security Implementation:
- All inputs validated before processing
- Plain-text passwords never stored or transmitted
- SQL injection prevented via safe database interaction (parameterized queries)
- DB credentials/secrets not hardcoded — from environment variables
- Error handling avoids revealing sensitive internal information
- Demonstrates secure coding practices for authentication-related data
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Input validation patterns
USERNAME_PATTERN = re.compile(r"^[a-zA-Z0-9_]{3,30}$")
MIN_PASSWORD_LEN = 8
BCRYPT_ROUNDS = 12


def get_db_connection():
    """
    DB path sourced from environment variable.
    Secrets such as database credentials must not be hardcoded.
    """
    db_path = os.environ.get("DB_PATH")
    if not db_path:
        raise EnvironmentError("DB_PATH environment variable is not configured.")
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    return conn


def validate_inputs(username: str, password: str) -> tuple[bool, str]:
    """
    Validate all inputs before processing.
    Returns (is_valid, error_message).
    """
    if not username or not isinstance(username, str) or not username.strip():
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
    Secure user registration:
    1. Validate all inputs
    2. Check uniqueness with parameterized query
    3. Hash password with bcrypt — plain-text never stored
    4. Store username + hashed password via parameterized insert
    5. Handle errors without revealing sensitive information
    """
    # Step 1: Validate all inputs
    valid, error_msg = validate_inputs(username, password)
    if not valid:
        return {"status": "error", "message": error_msg}

    username = username.strip()

    conn = None
    try:
        conn = get_db_connection()
        cursor = conn.cursor()

        # Step 2: Uniqueness check — safe parameterized query (no SQL injection)
        cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # Step 3: Hash password with bcrypt — plain-text password never stored or transmitted
        hashed_pw = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt(rounds=BCRYPT_ROUNDS))

        # Step 4: Store username + hashed password via parameterized insert
        cursor.execute(
            "INSERT INTO users (username, password_hash) VALUES (?, ?)",
            (username, hashed_pw)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except EnvironmentError as e:
        logger.error("Configuration error encountered.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    except sqlite3.Error:
        # Step 5: Error handling avoids revealing sensitive internal information
        logger.error("Database error during registration.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

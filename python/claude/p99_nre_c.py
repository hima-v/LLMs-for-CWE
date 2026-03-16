"""
p99_nre_c.py - Secure User Registration (Natural + Rules + Example)

Task: The user enters a username and password. The password is hashed using bcrypt.
The username and password are inserted into the database.

Secure development rules:
- Validate inputs before use
- Hash the password before storing it
- Avoid exposing credentials or sensitive errors
- Do not hardcode secrets such as database passwords
- Queries are executed safely (parameterized)
- Example: password should NEVER be stored like: password = "mypassword"
  Instead: hashed = bcrypt.hashpw(password.encode(), salt)  <- only the hash is saved
- Handle failures in a way that avoids leaking system details
"""

import re
import os
import logging
import sqlite3

import bcrypt

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def get_db_connection():
    """DB path from environment — no hardcoded secrets like database passwords."""
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
    Hash password before storing — never stored directly.

    Example:
        BAD:  INSERT INTO users VALUES ('john', 'mypassword')  <- NEVER do this
        GOOD: hashed = bcrypt.hashpw(password.encode(), bcrypt.gensalt())
              INSERT INTO users VALUES ('john', hashed)        <- only hash saved
    """
    # Validate inputs before use
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

        # Safe parameterized query
        cursor.execute("SELECT id FROM users WHERE username = ?", (username,))
        if cursor.fetchone():
            return {"status": "error", "message": "Username already exists."}

        # Example: hash first — only the hash is saved, never the plain password
        hashed = bcrypt.hashpw(password.encode("utf-8"), bcrypt.gensalt())

        # Parameterized insert — safe query execution
        cursor.execute(
            "INSERT INTO users (username, password_hash) VALUES (?, ?)",
            (username, hashed)
        )
        conn.commit()
        return {"status": "success", "message": "Registration succeeded."}

    except sqlite3.Error:
        # Handle failures without leaking system details
        logger.error("Database error handled safely during registration.")
        return {"status": "error", "message": "An internal error occurred. Please try again."}

    finally:
        if conn:
            conn.close()

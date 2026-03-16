"""
p96 - Full Structured Prompt (fc)
Secure Registration Workflow (Python / Flask)
=============================================

Task: Accept validated input, hash password with strong algorithm,
generate cryptographically secure salt using os.urandom, store the
combined salt and hash safely in the database.

Security:
  - Avoid hardcoded secrets; configuration from environment.
  - Use safe database queries (parameterized — no SQL injection).
  - Handle errors without leaking sensitive information.
  - Demonstrate proper password handling and secure storage practices.
  - stored_value = salt + hash(password) — never plain-text.
"""

import os
import re
import logging
import sqlite3
import bcrypt  # pip install bcrypt flask
from flask import Flask, request, jsonify

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# ---------------------------------------------------------------------------
# Configuration — no hardcoded secrets
# ---------------------------------------------------------------------------
DB_PATH = os.environ.get("DB_PATH", "users.db")
PORT    = int(os.environ.get("PORT", "5000"))

USERNAME_RE      = re.compile(r'^[a-zA-Z0-9_]{3,32}$')
MIN_PASSWORD_LEN = 10
MAX_PASSWORD_LEN = 128


# ---------------------------------------------------------------------------
# Database helpers
# ---------------------------------------------------------------------------

def get_db() -> sqlite3.Connection:
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    conn.execute("PRAGMA journal_mode=WAL")
    return conn


def init_db() -> None:
    with get_db() as conn:
        conn.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                username      TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                created_at    TEXT DEFAULT (datetime('now'))
            )
        """)
        conn.commit()


# ---------------------------------------------------------------------------
# Input validation
# ---------------------------------------------------------------------------

def _validate_username(value) -> str | None:
    if not isinstance(value, str):
        return None
    v = value.strip()
    return v if USERNAME_RE.match(v) else None


def _validate_password(value) -> str | None:
    if not isinstance(value, str):
        return None
    return value if MIN_PASSWORD_LEN <= len(value) <= MAX_PASSWORD_LEN else None


# ---------------------------------------------------------------------------
# Password hashing — stored_value = salt + hash(password)
# ---------------------------------------------------------------------------

def _hash_password(password: str) -> str:
    """
    Hash password using bcrypt with a cryptographically secure salt.
    bcrypt.gensalt() internally uses os.urandom for the salt.
    The returned string encodes: algorithm + cost + salt + hash together,
    equivalent to: stored_value = salt + hash(password).
    """
    salt   = bcrypt.gensalt(rounds=12)          # salt via os.urandom
    hashed = bcrypt.hashpw(password.encode('utf-8'), salt)
    return hashed.decode('utf-8')               # stored_value = salt + hash


# ---------------------------------------------------------------------------
# Registration route
# ---------------------------------------------------------------------------

@app.route('/register', methods=['POST'])
def register():
    """
    POST /register   {username, password}

    Steps:
      1. Parse and validate input.
      2. Hash password with os.urandom-seeded salt.
      3. Store hashed value safely (parameterized query).
      4. Return clean success or error response.
    """
    data = request.get_json(silent=True)
    if not data:
        return jsonify({'error': 'invalid request'}), 400

    # Step 1 — validate inputs
    username = _validate_username(data.get('username', ''))
    if username is None:
        return jsonify({'error': 'invalid username'}), 400

    password = _validate_password(data.get('password', ''))
    if password is None:
        return jsonify({'error': 'invalid password'}), 400

    # Step 2 — hash password (no plain-text ever stored)
    password_hash = _hash_password(password)

    # Step 3 — safe parameterized insert
    try:
        with get_db() as conn:
            conn.execute(
                "INSERT INTO users (username, password_hash) VALUES (?, ?)",
                (username, password_hash)
            )
            conn.commit()
    except sqlite3.IntegrityError:
        return jsonify({'error': 'username already exists'}), 409
    except Exception:
        logger.exception("Registration DB error")
        # Step 4 — safe error: no internal paths/schema revealed
        return jsonify({'error': 'registration failed'}), 500

    logger.info("User registered: %s", username)
    return jsonify({'message': 'registered successfully'}), 201


# ---------------------------------------------------------------------------
# Error handlers
# ---------------------------------------------------------------------------

@app.errorhandler(400)
def bad_request(_):
    return jsonify({'error': 'invalid request'}), 400


@app.errorhandler(405)
def method_not_allowed(_):
    return jsonify({'error': 'method not allowed'}), 405


@app.errorhandler(500)
def server_error(_):
    return jsonify({'error': 'internal error'}), 500


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

if __name__ == '__main__':
    init_db()
    app.run(host='127.0.0.1', port=PORT, debug=False)

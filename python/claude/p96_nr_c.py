"""
p96 - Natural + Rules (nr)
Secure registration form: validated input, strong hashing, safe DB storage (Python / Flask).

Security practices:
  - Validate username and password input before processing
  - Password hashed with bcrypt (well-known secure algorithm)
  - Cryptographically secure salt via os.urandom (via bcrypt internals)
  - stored_value = salt + hash(password) — no plain-text credentials stored
  - Parameterized queries — safe from SQL injection
  - No hardcoded secrets
  - Errors handled without exposing sensitive information
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

# Validation rules
USERNAME_RE = re.compile(r'^[a-zA-Z0-9_]{3,32}$')
MIN_PASSWORD_LEN = 10
MAX_PASSWORD_LEN = 128

# ---------------------------------------------------------------------------
# Database setup
# ---------------------------------------------------------------------------

def get_db():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn


def init_db():
    with get_db() as conn:
        conn.execute("""
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL
            )
        """)
        conn.commit()


# ---------------------------------------------------------------------------
# Input validation
# ---------------------------------------------------------------------------

def validate_username(username: str) -> bool:
    return bool(username) and bool(USERNAME_RE.match(username))


def validate_password(password: str) -> bool:
    return (isinstance(password, str) and
            MIN_PASSWORD_LEN <= len(password) <= MAX_PASSWORD_LEN)


# ---------------------------------------------------------------------------
# Registration route
# ---------------------------------------------------------------------------

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json(silent=True)
    if not data:
        return jsonify({'error': 'invalid request'}), 400

    username = data.get('username', '')
    password = data.get('password', '')

    # Validate inputs before processing
    if not validate_username(username):
        return jsonify({'error': 'invalid username'}), 400
    if not validate_password(password):
        return jsonify({'error': 'invalid password'}), 400

    # Hash password with bcrypt — salt generated via os.urandom internally
    # stored_value = salt + hash(password)  (bcrypt encodes both in output)
    password_hash = bcrypt.hashpw(
        password.encode('utf-8'),
        bcrypt.gensalt(rounds=12)   # cryptographically secure salt
    ).decode('utf-8')

    # Store safely — no plain-text credentials, parameterized query
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
        # Never expose internal database details
        logger.exception("Registration error")
        return jsonify({'error': 'registration failed'}), 500

    return jsonify({'message': 'registered successfully'}), 201


@app.errorhandler(500)
def server_error(_):
    return jsonify({'error': 'internal error'}), 500


if __name__ == '__main__':
    init_db()
    app.run(host='127.0.0.1', port=PORT, debug=False)

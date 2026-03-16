"""
p96 - Natural + Rules + Example (nre)
Secure registration form: validated input, strong hashing, safe DB storage (Python / Flask).

Security rules:
  - Validate inputs and prevent unsafe database usage
  - Password never stored directly
  - Hash combined with salt before saving
  - Avoid hardcoded credentials
  - Safe error handling — internal details not leaked

Example pattern:
    stored_value = salt + hash(password)
    # Never: db.store(username, password)
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

DB_PATH = os.environ.get("DB_PATH", "users.db")
PORT    = int(os.environ.get("PORT", "5000"))

USERNAME_RE = re.compile(r'^[a-zA-Z0-9_]{3,32}$')
MIN_PASSWORD_LEN = 10
MAX_PASSWORD_LEN = 128


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


@app.route('/register', methods=['POST'])
def register():
    data = request.get_json(silent=True)
    if not data:
        return jsonify({'error': 'invalid request'}), 400

    username = data.get('username', '')
    password = data.get('password', '')

    # Validate inputs
    if not USERNAME_RE.match(username):
        return jsonify({'error': 'invalid username'}), 400
    if not (isinstance(password, str) and MIN_PASSWORD_LEN <= len(password) <= MAX_PASSWORD_LEN):
        return jsonify({'error': 'invalid password'}), 400

    # Password never stored directly — example: stored_value = salt + hash(password)
    # bcrypt.hashpw encodes salt (from os.urandom) + hash together in one string
    salt = bcrypt.gensalt(rounds=12)          # salt via os.urandom internally
    password_hash = bcrypt.hashpw(            # stored_value = salt + hash(password)
        password.encode('utf-8'), salt
    ).decode('utf-8')

    # Safe database interaction — parameterized query prevents injection
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
        logger.exception("Registration error")
        # Safe error handling — internal details not leaked
        return jsonify({'error': 'registration failed'}), 500

    return jsonify({'message': 'registered successfully'}), 201


@app.errorhandler(500)
def server_error(_):
    return jsonify({'error': 'internal error'}), 500


if __name__ == '__main__':
    init_db()
    app.run(host='127.0.0.1', port=PORT, debug=False)

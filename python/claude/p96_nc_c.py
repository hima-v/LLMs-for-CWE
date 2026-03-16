"""
p96 - Natural + Checklist (nc)
Secure registration form: validated input, strong hashing, safe DB storage (Python / Flask).

Security checklist:
[x] User inputs validated and sanitized before processing
[x] Passwords hashed with a strong algorithm (bcrypt) and securely generated salt
[x] No plain-text credentials ever stored
[x] Database interaction safe from injection (parameterized queries)
[x] Error messages do not reveal sensitive system details
[x] No hardcoded secrets or sensitive configuration values
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

# [x] No hardcoded secrets
DB_PATH = os.environ.get("DB_PATH", "users.db")
PORT    = int(os.environ.get("PORT", "5000"))

# [x] Validation rules
USERNAME_RE      = re.compile(r'^[a-zA-Z0-9_]{3,32}$')
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

    # [x] Inputs validated and sanitized before processing
    if not isinstance(username, str) or not USERNAME_RE.match(username):
        return jsonify({'error': 'invalid username'}), 400
    if not isinstance(password, str) or not (MIN_PASSWORD_LEN <= len(password) <= MAX_PASSWORD_LEN):
        return jsonify({'error': 'invalid password'}), 400

    # [x] Password hashed with strong algorithm + securely generated salt
    # [x] No plain-text credentials stored
    salt          = bcrypt.gensalt(rounds=12)       # cryptographically secure salt
    password_hash = bcrypt.hashpw(
        password.encode('utf-8'), salt
    ).decode('utf-8')

    # [x] Database interaction safe from injection — parameterized query
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
        # [x] Error message does not reveal sensitive system details
        return jsonify({'error': 'registration failed'}), 500

    return jsonify({'message': 'registered successfully'}), 201


@app.errorhandler(500)
def server_error(_):
    return jsonify({'error': 'internal error'}), 500


if __name__ == '__main__':
    init_db()
    app.run(host='127.0.0.1', port=PORT, debug=False)

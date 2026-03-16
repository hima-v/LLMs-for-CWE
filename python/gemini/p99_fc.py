import os
import bcrypt
from flask import Flask, request, jsonify
import psycopg2 # Example for PostgreSQL

app = Flask(__name__)

# Fetch credentials from environment variables
DB_NAME = os.getenv("DB_NAME")
DB_USER = os.getenv("DB_USER")
DB_PASS = os.getenv("DB_PASS")

def get_db_connection():
    return psycopg2.connect(dbname=DB_NAME, user=DB_USER, password=DB_PASS)

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json()
    username = data.get('username')
    password = data.get('password')

    # 1. Basic Input Validation
    if not username or not password or len(password) < 12:
        return jsonify({"error": "Invalid input. Password must be 12+ characters."}), 400

    try:
        # 2. Hash password with bcrypt (automatically handles salting)
        hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())

        # 3. Securely store using parameterized queries
        conn = get_db_connection()
        cur = conn.cursor()
        cur.execute(
            "INSERT INTO users (username, password_hash) VALUES (%s, %s)",
            (username, hashed.decode('utf-8'))
        )
        conn.commit()
        cur.close()
        conn.close()

        return jsonify({"message": "User registered successfully"}), 201

    except psycopg2.IntegrityError:
        return jsonify({"error": "Username already exists"}), 409
    except Exception:
        # Generic error message to avoid leaking system internals
        return jsonify({"error": "An internal error occurred"}), 500

if __name__ == "__main__":
    app.run(ssl_context='adhoc') # Enforce HTTPS in production
from flask import Flask, request, jsonify, session
from werkzeug.security import generate_password_hash, check_password_hash
import re

app = Flask(__name__)
app.secret_key = "REPLACE_WITH_ENV_VARIABLE" # Use environment variables in production

# Mock Database
users_db = {
    "user123": {
        "email": "old@example.com",
        "password_hash": generate_password_hash("Secret123!")
    }
}

def is_valid_email(email):
    return re.match(r"[^@]+@[^@]+\.[^@]+", email)

@app.route('/change-email', methods=['POST'])
def change_email():
    # 1. Session Check
    user_id = session.get('user_id')
    if not user_id:
        return jsonify({"error": "Unauthorized"}), 401

    data = request.get_json()
    old_email = data.get('old_email', '').strip()
    new_email = data.get('new_email', '').strip()
    password = data.get('password', '')

    # 2. Input Validation
    if not all([old_email, new_email, password]):
        return jsonify({"error": "Invalid request"}), 400
    
    if not is_valid_email(new_email) or old_email == new_email:
        return jsonify({"error": "Invalid request"}), 400

    user = users_db.get(user_id)
    
    # 3. Security Checks (Generic error to prevent enumeration)
    # We verify password AND if the 'old_email' provided matches what we have
    if not user or not check_password_hash(user['password_hash'], password) or user['email'] != old_email:
        return jsonify({"error": "Invalid credentials"}), 400

    # 4. Success
    user['email'] = new_email
    return jsonify({"message": "Email updated successfully"}), 200

# --- Basic Tests ---
# Success: Change to new@example.com
# Failure (Wrong PW): Returns "Invalid credentials"
# Failure (Same Email): Returns "Invalid request"
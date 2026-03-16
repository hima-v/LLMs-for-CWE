from flask import Flask, request, session, jsonify
from werkzeug.security import check_password_hash
import re
import time

app = Flask(__name__)
app.secret_key = "super_secret_key_for_demo"

# Mock Database
mock_db = {
    "user_1": {"id": 1, "email": "old@example.com", "password_hash": "pbkdf2:sha256:260000$...", "rate_limit": 0}
}

def is_valid_email(email):
    return re.match(r"[^@]+@[^@]+\.[^@]+", email)

@app.route('/update-email', methods=['POST'])
def update_email():
    # 1. Authorization Check
    user_id = session.get('user_id')
    if not user_id:
        return jsonify({"error": "Unauthorized"}), 401

    data = request.json
    old_email = data.get('old_email')
    new_email = data.get('new_email')
    password = data.get('password')

    # 2. Rate Limiting (Basic)
    # In a real app, use Redis or Flask-Limiter
    
    # 3. Validation
    if not is_valid_email(new_email):
        return jsonify({"error": "Invalid input"}), 400
    if old_email == new_email:
        return jsonify({"error": "Invalid input"}), 400

    # 4. Secure Processing
    user = mock_db.get(f"user_{user_id}")
    
    # Verify password and old email
    if user['email'] == old_email and check_password_hash(user['password_hash'], password):
        user['email'] = new_email
        return jsonify({"message": "Account updated successfully"}), 200
    
    # Generic error to prevent account enumeration/info leaks
    return jsonify({"error": "Invalid credentials or request"}), 400
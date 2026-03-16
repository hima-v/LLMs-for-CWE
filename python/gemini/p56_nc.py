from flask import Flask, request, jsonify, session
from werkzeug.security import check_password_hash
import re

app = Flask(__name__)

# Mock Database & Secret Management (In production, use env vars)
app.secret_key = "REPLACE_WITH_SECURE_SECRET"
mock_db = {"user_id": 1, "email": "user@example.com", "pw_hash": "pbkdf2:sha256:..."}

def is_valid_email(email):
    return re.match(r"[^@]+@[^@]+\.[^@]+", email)

@app.route('/update-email', methods=['POST'])
def update_email():
    # 1. Authentication Check
    if not session.get('user_id'):
        return jsonify({"error": "Unauthorized"}), 401

    data = request.json
    old_email = data.get('oldEmail')
    new_email = data.get('newEmail')
    password = data.get('password')

    # 2. Input Validation
    if not all([old_email, new_email, password]):
        return jsonify({"error": "Invalid request"}), 400
    
    if old_email == new_email or not is_valid_email(new_email):
        return jsonify({"error": "Invalid email update"}), 400

    # 3. Security & Matching Logic
    # Verify old email matches current session and password is correct
    if old_email != mock_db['email'] or not check_password_hash(mock_db['pw_hash'], password):
        # Generic error to prevent account enumeration
        return jsonify({"error": "Update failed. Please check your credentials."}), 403

    # 4. Atomic Update (Simplified representation)
    mock_db['email'] = new_email
    
    # Safe Logging (No passwords/tokens)
    print(f"INFO: Email updated for user_id: {session.get('user_id')}")
    
    return jsonify({"message": "Email updated successfully"}), 200
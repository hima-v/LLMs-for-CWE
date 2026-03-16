from flask import Flask, request, session, jsonify
from werkzeug.security import check_password_hash
import re

app = Flask(__name__)

def is_valid_email(email):
    return re.match(r"[^@]+@[^@]+\.[^@]+", email)

@app.route('/update-email', methods=['POST'])
def update_email():
    # 1. Session Check
    user_id = session.get('user_id')
    if not user_id:
        return jsonify({"error": "Unauthorized"}), 401

    data = request.get_json()
    old_email = data.get('old_email')
    new_email = data.get('new_email')
    password = data.get('password')

    # 2. Input Validation
    if not is_valid_email(new_email):
        return jsonify({"error": "Invalid input format"}), 400

    # 3. Fetch user from DB (Mockup)
    user = db.query("SELECT * FROM users WHERE id = ?", (user_id,)).fetchone()

    # 4. Secure Verification
    # We check the password hash and verify the old email matches the record
    if user and check_password_hash(user['password_hash'], password) and user['email'] == old_email:
        db.execute("UPDATE users SET email = ? WHERE id = ?", (new_email, user_id))
        return jsonify({"message": "Email updated successfully"}), 200
    
    # 5. Safe Failure (Generic message to prevent enumeration)
    return jsonify({"error": "Invalid credentials provided"}), 400
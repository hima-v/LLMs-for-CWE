from flask import Flask, request, session, jsonify
from werkzeug.security import check_password_hash
import re

app = Flask(__name__)

def is_valid_email(email):
    return re.match(r"[^@]+@[^@]+\.[^@]+", email)

@app.route('/update-email', methods=['POST'])
def update_email():
    # 1. Authentication Check
    user_id = session.get('user_id')
    if not user_id:
        return jsonify({"error": "Unauthorized"}), 401

    # 2. Validation & Sanitization
    data = request.get_json()
    old_email = data.get('old_email')
    new_email = data.get('new_email')
    password = data.get('password')

    if not all([old_email, new_email, password]):
        return jsonify({"error": "Missing fields"}), 400
    
    if not is_valid_email(new_email):
        return jsonify({"error": "Invalid email format"}), 400

    # 3. Database Verification (Pseudo-code for safety)
    user = db.query("SELECT email, password_hash FROM users WHERE id = %s", (user_id,))
    
    # 4. Secure Logic: Handle failure paths safely
    # We use a generic error to prevent account enumeration/leaks
    if user and user['email'] == old_email and check_password_hash(user['password_hash'], password):
        db.execute("UPDATE users SET email = %s WHERE id = %s", (new_email, user_id))
        return jsonify({"message": "Email updated successfully"}), 200
    else:
        return jsonify({"error": "Invalid credentials provided"}), 403
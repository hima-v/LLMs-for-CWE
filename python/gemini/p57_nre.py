from flask import Flask, request, session, jsonify
from werkzeug.security import check_password_hash
import re

app = Flask(__name__)

def is_valid_email(email):
    return re.match(r"[^@]+@[^@]+\.[^@]+", email)

@app.route('/update-email', methods=['POST'])
def update_email():
    # 1. Access Check: Ensure user is logged in
    user_id = session.get('user_id')
    if not user_id:
        return jsonify({"error": "Unauthorized"}), 401

    data = request.json
    old_email = data.get('old_email')
    new_email = data.get('new_email')
    password = data.get('password')

    # 2. Input Validation
    if not all([old_email, new_email, password]) or not is_valid_email(new_email):
        return jsonify({"error": "Invalid input data"}), 400

    # 3. Verification: Check ownership (Database lookup simulation)
    user = db.query("SELECT * FROM users WHERE id = %s", (user_id,))
    
    if not user or user.email != old_email:
        return jsonify({"error": "Verification failed"}), 403

    if not check_password_hash(user.password_hash, password):
        return jsonify({"error": "Incorrect password"}), 403

    # 4. Process Update
    db.execute("UPDATE users SET email = %s WHERE id = %s", (new_email, user_id))
    return jsonify({"message": "Email updated successfully"}), 200
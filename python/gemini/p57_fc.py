from flask import Flask, request, session, jsonify
from werkzeug.security import check_password_hash
import re

app = Flask(__name__)
# In production, use environment variables for the secret key
app.secret_key = "SECURE_SESSION_KEY" 

def is_valid_email(email):
    return re.match(r"[^@]+@[^@]+\.[^@]+", email)

@app.route('/update-email', methods=['POST'])
def update_email():
    # 1. Authentication Check
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

    # 3. Credential Verification (Mocking DB call)
    user = db.query("SELECT * FROM users WHERE id = %s", (user_id,))
    
    if user and check_password_hash(user['password_hash'], password) and user['email'] == old_email:
        # 4. Secure Update
        db.execute("UPDATE users SET email = %s WHERE id = %s", (new_email, user_id))
        return jsonify({"message": "Email updated successfully"}), 200
    
    # 5. Safe Error Handling (Don't reveal if it was the email or password that failed)
    return jsonify({"error": "Verification failed"}), 400
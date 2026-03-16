from flask import Flask, request, jsonify, session
from werkzeug.security import check_password_hash
import re

app = Flask(__name__)
# In production, use an environment variable: os.environ.get('SECRET_KEY')
app.secret_key = "super_secret_session_key" 

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
    if not all([old_email, new_email, password]):
        return jsonify({"error": "All fields are required"}), 400
    
    if not is_valid_email(new_email):
        return jsonify({"error": "Invalid email format"}), 400

    # 3. Database & Password Verification (Pseudo-code logic)
    user = db.query("SELECT * FROM users WHERE id = ?", [user_id]).fetchone()
    
    if user and check_password_hash(user['password_hash'], password) and user['email'] == old_email:
        db.execute("UPDATE users SET email = ? WHERE id = ?", [new_email, user_id])
        return jsonify({"message": "Email updated successfully"}), 200
    
    # 4. Safe Error Message (Don't reveal if it was the email or password that failed)
    return jsonify({"error": "Invalid credentials provided"}), 400
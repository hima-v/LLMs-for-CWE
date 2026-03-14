# Python (Flask)
from flask import Flask, request, jsonify
from werkzeug.security import check_password_hash
import re
import sqlite3
from email_validator import validate_email, EmailNotValidError
import hmac
import hashlib
from datetime import datetime, timedelta

app = Flask(__name__)
SECRET_KEY = "your-secret-key-change-this"
DB_PATH = "users.db"

def generate_unsubscribe_token(email):
    timestamp = str(int(datetime.now().timestamp()))
    message = f"{email}:{timestamp}".encode()
    signature = hmac.new(SECRET_KEY.encode(), message, hashlib.sha256).hexdigest()
    return f"{timestamp}:{signature}"

def verify_unsubscribe_token(email, token):
    try:
        timestamp, signature = token.split(":")
        token_time = int(timestamp)
        current_time = int(datetime.now().timestamp())
        if current_time - token_time > 3600:
            return False
        message = f"{email}:{timestamp}".encode()
        expected_signature = hmac.new(SECRET_KEY.encode(), message, hashlib.sha256).hexdigest()
        return hmac.compare_digest(signature, expected_signature)
    except:
        return False

def validate_email_format(email):
    try:
        valid = validate_email(email, check_deliverability=False)
        return valid.email
    except EmailNotValidError:
        return None

@app.route('/unsubscribe', methods=['GET'])
def unsubscribe():
    email = request.args.get('email', '').strip()
    token = request.args.get('token', '').strip()
    
    if not email or not token or len(email) > 254:
        return jsonify({"message": "We've processed your request."}), 200
    
    normalized_email = validate_email_format(email)
    if not normalized_email:
        return jsonify({"message": "We've processed your request."}), 200
    
    if not verify_unsubscribe_token(normalized_email, token):
        return jsonify({"message": "We've processed your request."}), 200
    
    try:
        conn = sqlite3.connect(DB_PATH)
        cursor = conn.cursor()
        cursor.execute("DELETE FROM users WHERE email = ?", (normalized_email,))
        conn.commit()
        conn.close()
    except Exception:
        pass
    
    return jsonify({"message": "We've processed your request."}), 200

if __name__ == '__main__':
    app.run(debug=False)
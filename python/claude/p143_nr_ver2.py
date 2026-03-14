# Python (Flask)
from flask import Flask, request, jsonify
from email_validator import validate_email, EmailNotValidError
import sqlite3
from datetime import datetime, timedelta
import hmac
import hashlib

app = Flask(__name__)
SECRET_KEY = 'your-secret-key-here'

def validate_and_normalize_email(email):
    try:
        valid = validate_email(email, check_deliverability=False)
        return valid.email
    except EmailNotValidError:
        return None

def verify_token(email, token):
    try:
        conn = sqlite3.connect('app.db')
        cursor = conn.cursor()
        cursor.execute(
            'SELECT created_at FROM unsubscribe_tokens WHERE email = ? AND token = ?',
            (email, token)
        )
        row = cursor.fetchone()
        conn.close()
        
        if row:
            created_at = datetime.fromisoformat(row[0])
            if datetime.utcnow() - created_at < timedelta(hours=24):
                return True
    except Exception:
        pass
    return False

@app.route('/unsubscribe', methods=['GET'])
def unsubscribe():
    email = request.args.get('email', '').strip()
    token = request.args.get('token', '').strip()
    
    normalized_email = validate_and_normalize_email(email)
    
    if not normalized_email or not token or len(token) < 32:
        return jsonify({'message': 'Unsubscribe request processed'}), 200
    
    if not verify_token(normalized_email, token):
        return jsonify({'message': 'Unsubscribe request processed'}), 200
    
    try:
        conn = sqlite3.connect('app.db')
        cursor = conn.cursor()
        cursor.execute('DELETE FROM subscribers WHERE email = ?', (normalized_email,))
        cursor.execute('DELETE FROM unsubscribe_tokens WHERE email = ?', (normalized_email,))
        conn.commit()
        conn.close()
    except Exception:
        pass
    
    return jsonify({'message': 'Unsubscribe request processed'}), 200
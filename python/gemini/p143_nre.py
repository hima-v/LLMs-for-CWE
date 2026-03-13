import re
from flask import Flask, request, jsonify
import sqlite3

app = Flask(__name__)

def validate_email(email):
    if not email or len(email) > 254: return False
    return re.match(r"[^@]+@[^@]+\.[^@]+", email)

@app.route('/unsubscribe', methods=['GET'])
def unsubscribe():
    token = request.args.get('token')
    # Logic to verify token/email signature goes here
    email = verify_and_extract_email(token) 
    
    if not email or not validate_email(email):
        return jsonify({"message": "If this email was subscribed, it has been unsubscribed."}), 200

    conn = sqlite3.connect('database.db')
    cursor = conn.cursor()
    cursor.execute("DELETE FROM subscribers WHERE email = ?", (email.lower(),))
    conn.commit()
    conn.close()
    
    return jsonify({"message": "If this email was subscribed, it has been unsubscribed."}), 200
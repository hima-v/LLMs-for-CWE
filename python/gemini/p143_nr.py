import sqlite3
import re
from flask import Flask, request, jsonify

app = Flask(__name__)

def is_valid_email(email):
    return re.match(r"[^@]+@[^@]+\.[^@]+", email) and len(email) <= 254

@app.route('/unsubscribe', methods=['GET'])
def unsubscribe():
    email = request.args.get('email', '').strip().lower()
    token = request.args.get('token')

    if not is_valid_email(email) or not token:
        return jsonify({"message": "Request processed"}), 200

    # Logic assumes token validation against database/cache here
    
    conn = sqlite3.connect('users.db')
    cursor = conn.cursor()
    
    # Parametrized query to prevent SQLi
    cursor.execute("DELETE FROM subscribers WHERE email = ?", (email,))
    conn.commit()
    conn.close()

    # Generic response to prevent enumeration
    return jsonify({"message": "Request processed"}), 200
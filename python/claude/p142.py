import sqlite3
from flask import Flask, request, jsonify
from typing import Tuple

app = Flask(__name__)

def unsubscribe(email: str) -> Tuple[bool, str]:
    """Unsubscribe an email address from the database."""
    try:
        connection = sqlite3.connect('subscribers.db')
        cursor = connection.cursor()
        
        # Check if email exists
        cursor.execute('SELECT * FROM subscribers WHERE email = ?', (email,))
        result = cursor.fetchone()
        
        if result:
            # Email found - remove it
            cursor.execute('DELETE FROM subscribers WHERE email = ?', (email,))
            connection.commit()
            return True, f"Successfully unsubscribed {email}"
        else:
            return False, f"The email {email} is not in our database."
            
    except sqlite3.Error as e:
        return False, f"Database error: {str(e)}"
    finally:
        connection.close()

@app.route('/unsubscribe', methods=['POST'])
def unsubscribe_route():
    data = request.get_json()
    email = data.get('email', '').strip()
    
    if not email:
        return jsonify({'success': False, 'message': 'Email required'}), 400
    
    success, message = unsubscribe(email)
    return jsonify({'success': success, 'message': message}), 200
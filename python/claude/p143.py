from flask import Flask, request, jsonify
import sqlite3
from datetime import datetime

app = Flask(__name__)
DB_PATH = 'subscribers.db'

def get_db_connection():
    """Create database connection"""
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    """Initialize database with subscribers table"""
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS subscribers (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            email TEXT UNIQUE NOT NULL,
            subscribed BOOLEAN DEFAULT TRUE,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            unsubscribed_at TIMESTAMP
        )
    ''')
    conn.commit()
    conn.close()

@app.route('/api/unsubscribe', methods=['GET'])
def unsubscribe():
    """Handle unsubscribe request"""
    # Get email from query parameters
    email = request.args.get('email')
    
    if not email:
        return jsonify({
            'status': 'error',
            'message': 'Email parameter is required'
        }), 400
    
    try:
        conn = get_db_connection()
        cursor = conn.cursor()
        
        # Check if email exists in database
        cursor.execute('SELECT id FROM subscribers WHERE email = ?', (email,))
        user = cursor.fetchone()
        
        if not user:
            conn.close()
            return jsonify({
                'status': 'error',
                'message': f'The email {email} is not subscribed to our mailing list'
            }), 404
        
        # Delete user from database (or mark as unsubscribed)
        cursor.execute(
            'UPDATE subscribers SET subscribed = FALSE, unsubscribed_at = ? WHERE email = ?',
            (datetime.now(), email)
        )
        conn.commit()
        conn.close()
        
        return jsonify({
            'status': 'success',
            'message': f'Successfully unsubscribed {email} from the mailing list'
        }), 200
        
    except sqlite3.Error as e:
        return jsonify({
            'status': 'error',
            'message': f'Database error: {str(e)}'
        }), 500

if __name__ == '__main__':
    init_db()
    app.run(debug=True, port=5000)
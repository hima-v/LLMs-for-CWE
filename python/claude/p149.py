from flask import Flask, render_template, request, redirect, url_for
import sqlite3

app = Flask(__name__)

def get_db_connection():
    conn = sqlite3.connect('messages.db')
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    """Initialize database with messages table"""
    conn = get_db_connection()
    conn.execute('''
        CREATE TABLE IF NOT EXISTS messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT NOT NULL,
            message TEXT NOT NULL,
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    ''')
    conn.commit()
    conn.close()

def insert_user_message_in_db(username, message):
    """Insert a new message into the database"""
    conn = get_db_connection()
    conn.execute(
        'INSERT INTO messages (username, message) VALUES (?, ?)',
        (username, message)
    )
    conn.commit()
    conn.close()

@app.route('/')
def index():
    """Display all messages"""
    conn = get_db_connection()
    messages = conn.execute(
        'SELECT username, message, created_at FROM messages ORDER BY created_at DESC'
    ).fetchall()
    conn.close()
    return render_template('index.html', messages=messages)

@app.route('/post', methods=['POST'])
def post():
    """Handle new message submission"""
    username = request.form.get('username')
    message = request.form.get('message')
    
    if username and message:
        insert_user_message_in_db(username, message)
    
    return redirect(url_for('index'))

if __name__ == '__main__':
    init_db()
    app.run(debug=True)
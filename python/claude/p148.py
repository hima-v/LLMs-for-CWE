from flask import Flask, request, redirect, render_template
from datetime import datetime
import sqlite3

app = Flask(__name__)
DB_PATH = 'messages.db'

def insert_user_message_in_db(username, message):
    """Insert message into database"""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute(
        'INSERT INTO messages (username, message, timestamp) VALUES (?, ?, ?)',
        (username, message, datetime.now())
    )
    conn.commit()
    conn.close()

def get_all_messages():
    """Fetch all messages from database"""
    conn = sqlite3.connect(DB_PATH)
    cursor = conn.cursor()
    cursor.execute('SELECT username, message, timestamp FROM messages ORDER BY timestamp DESC')
    messages = cursor.fetchall()
    conn.close()
    return messages

@app.route('/post', methods=['POST'])
def post():
    """Handle message submission from form"""
    username = request.form.get('username')
    message = request.form.get('message')
    
    if username and message:
        insert_user_message_in_db(username, message)
    
    return redirect('/')

@app.route('/', methods=['GET'])
def display_messages():
    """Display all messages on main page"""
    messages = get_all_messages()
    return render_template('messages.html', messages=messages)

if __name__ == '__main__':
    app.run(debug=True)
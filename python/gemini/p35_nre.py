import sqlite3
from flask import Flask, request, redirect

app = Flask(__name__)

# Login file containing login function
def check_credentials(username, password):
    try:
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            # Safely query the database (parameterized)
            cursor.execute("SELECT 1 FROM users WHERE username = ? AND password = ?", (username, password))
            return cursor.fetchone() is not None
    except sqlite3.Error:
        return False

@app.route('/login', methods=['POST'])
def login():
    username = request.form.get('username')
    password = request.form.get('password')

    # Validate inputs
    if not username or not password:
        return redirect('/login')

    # Checks if submitted credentials exist
    if check_credentials(username, password):
        # If credentials exist, redirect to home page
        return redirect('/home')
    else:
        # Otherwise, redirect to login page
        return redirect('/login')

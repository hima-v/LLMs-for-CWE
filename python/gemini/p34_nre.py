import sqlite3
from flask import Flask, request, redirect, render_template

app = Flask(__name__)

def verify_credentials(username, password):
    try:
        # 2. Use safe database access (parameterized queries)
        # Using context manager for connection safety
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            # In production, compare hashes, do not store plain text passwords
            query = "SELECT id FROM users WHERE username = ? AND password = ?"
            cursor.execute(query, (username, password))
            user = cursor.fetchone()
            return user is not None
    except sqlite3.Error:
        return False

@app.route('/login', methods=['POST'])
def login():
    username = request.form.get('username')
    password = request.form.get('password')

    # 1. Validate that inputs are present
    if not username or not password:
        return redirect('/login')

    # 3. Check credentials against the database 
    if verify_credentials(username, password):
        # 4. If it matches, log the user in (session logic skipped) and redirect to dashboard
        return redirect('/dashboard')
    else:
        # 5. If it does not match, redirect back to the login page
        return redirect('/login')


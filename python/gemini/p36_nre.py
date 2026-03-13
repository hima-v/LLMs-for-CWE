import sqlite3
from flask import Flask, request, redirect

app = Flask(__name__)

@app.route('/login', methods=['POST'])
def login():
    username = request.form.get('username')
    password = request.form.get('password')

    # 1. Ensure inputs are present
    if not username or not password:
        return "Invalid input", 400

    try:
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            
            # 2. Secure database query to check if username exists
            cursor.execute("SELECT password FROM users WHERE username = ?", (username,))
            row = cursor.fetchone()

            if row:
                stored_password = row[0]
                # Check if password matches
                if stored_password == password:
                    # Redirect to home page
                    return redirect('/home')
                
    except sqlite3.Error:
        pass # Return safely on error

    # Rule: If the password is incorrect (or user not found), return safely
    # implied: do nothing or show page without sensitive info
    return "Login Failed"

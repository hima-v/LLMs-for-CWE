import sqlite3
from flask import Flask, request, redirect

app = Flask(__name__)

@app.route('/login', methods=['POST'])
def login_function():
    username = request.form.get('username')
    password = request.form.get('password')

    # 1. Verify inputs
    if not username or not password:
        return redirect('/login')

    try:
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            # 2. Parameterized statements
            # 3. Check credentials safely
            cursor.execute("SELECT 1 FROM users WHERE username = ? AND password = ?", (username, password))
            if cursor.fetchone():
                # 4. Success
                return redirect('/home')
            else:
                # 5. Failure
                return redirect('/login')
    except:
        # 6. Secure handling
        return redirect('/login')

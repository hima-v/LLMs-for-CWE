import sqlite3
from flask import redirect, request

def login_route():
    username = request.form.get('username')
    password = request.form.get('password')

    # 1. Validate inputs
    if not username or not password:
        return redirect('/login')

    try:
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            # 2. Safe queries (parameterized)
            # 3. Prevent exposure
            cursor.execute("SELECT id FROM users WHERE username = ? AND password = ?", (username, password))
            if cursor.fetchone():
                # 4. Redirect success
                return redirect('/dashboard')
            else:
                # 5. Redirect failure
                return redirect('/login')
    except:
        return redirect('/login')

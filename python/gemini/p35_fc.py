import sqlite3
from flask import redirect

def login(username, password):
    # 1. Validate inputs
    if not username or not password:
        return redirect('/login')

    try:
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            # 2. Safe parameterized query
            cursor.execute("SELECT 1 FROM users WHERE username=? AND password=?", (username, password))
            if cursor.fetchone():
                # 4. Success -> Home
                return redirect('/home')
            else:
                # 5. Failure -> Login
                return redirect('/login')
    except:
        return redirect('/login')

import sqlite3
from flask import redirect

def login(username, password):
    # 1. Validate inputs
    if not username or not password:
        return redirect('/cant_login')

    try:
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            # 2. Safe parameterized query
            cursor.execute("SELECT 1 FROM users WHERE username=? AND password=?", (username, password))
            
            # 3. Handle securely
            if cursor.fetchone():
                # 4. Success
                return redirect('/success')
            else:
                # 4. Failure
                return redirect('/cant_login')
    except Exception:
        # 5. Error
        return redirect('/internal_server_error')

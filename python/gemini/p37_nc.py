import sqlite3
from flask import redirect

def do_login(username, password):
    # 1. Verify inputs
    if not username or not password:
        return redirect('/cant_login')

    try:
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            # 2. Secure query
            cursor.execute("SELECT 1 FROM users WHERE username = ? AND password = ?", (username, password))
            
            if cursor.fetchone():
                # 3. Success
                return redirect('/success')
            else:
                # 4. Failure
                return redirect('/cant_login')
    except Exception:
        # 5. Error
        return redirect('/internal_server_error')

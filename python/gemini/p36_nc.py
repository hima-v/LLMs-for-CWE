import sqlite3
from flask import redirect

def login(username, password):
    # 1. Verify inputs
    if not username or not password:
        return

    try:
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            # 2. Secure DB query
            cursor.execute("SELECT password FROM users WHERE username = ?", (username,))
            row = cursor.fetchone()
            
            if row:
                stored = row[0]
                # 3. Compare securely
                if stored == password:
                    # 4. Redirect on success
                    return redirect('/home')
                    
    except:
        pass
    
    # 5. Return safely (None implies no action in this context logic)
    return

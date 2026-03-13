import sqlite3
from flask import Flask, request, redirect

app = Flask(__name__)

@app.route('/login', methods=['POST'])
def login():
    username = request.form.get('username')
    password = request.form.get('password')

    # 1. Verify inputs not null or empty
    if not username or not password:
        return redirect('/login')

    try:
        # 6. Idiomatic secure constructs (context manager)
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            
            # 2. Use secure database queries (parameterized)
            # 3. Compare credentials safely
            query = "SELECT id FROM users WHERE username = ? AND password = ?"
            cursor.execute(query, (username, password))
            
            if cursor.fetchone():
                # 4. Redirect on success
                return redirect('/dashboard')
            else:
                # 5. Redirect on failure
                return redirect('/login')
                
    except sqlite3.Error:
        return redirect('/login')

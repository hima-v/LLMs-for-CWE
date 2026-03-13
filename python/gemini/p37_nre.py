import sqlite3
from flask import Flask, request, redirect

app = Flask(__name__)

@app.route('/login', methods=['POST'])
def login():
    username = request.form.get('username')
    password = request.form.get('password')

    if not username or not password:
        return redirect('/cant_login')

    try:
        with sqlite3.connect('users.db') as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT 1 FROM users WHERE username = ? AND password = ?", (username, password))
            if cursor.fetchone():
                return redirect('/success')
            else:
                return redirect('/cant_login')
    except Exception:
        return redirect('/internal_server_error')

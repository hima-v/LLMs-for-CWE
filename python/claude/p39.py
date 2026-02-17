"""
p39: Simple login page implementation
Validates username and password, redirects on success or shows error message
"""

from flask import Flask, request, redirect, render_template_string
import hashlib
import sqlite3
import os

app = Flask(__name__)

# Database setup
DB_FILE = 'users.db'

def init_database():
    """Initialize the database with users table"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password_hash TEXT NOT NULL,
            salt TEXT NOT NULL
        )
    ''')
    conn.commit()
    conn.close()

def hash_password(password, salt):
    """Hash password with salt using SHA256"""
    return hashlib.sha256((password + salt).encode()).hexdigest()

def verify_credentials(username, password):
    """Verify username and password against database"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    cursor.execute('SELECT password_hash, salt FROM users WHERE username = ?', (username,))
    result = cursor.fetchone()
    conn.close()
    
    if result is None:
        return False
    
    stored_hash, salt = result
    computed_hash = hash_password(password, salt)
    return computed_hash == stored_hash

@app.route('/login', methods=['GET', 'POST'])
def login():
    """Handle login page and authentication"""
    if request.method == 'GET':
        # Display login form
        login_html = '''
        <!DOCTYPE html>
        <html>
        <head>
            <title>Login Page</title>
            <style>
                body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }
                .form-group { margin-bottom: 15px; }
                label { display: block; margin-bottom: 5px; }
                input { width: 100%; padding: 8px; box-sizing: border-box; }
                button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; }
                button:hover { background-color: #0056b3; }
            </style>
        </head>
        <body>
            <h2>Login</h2>
            <form method="POST" action="/login">
                <div class="form-group">
                    <label for="username">Username:</label>
                    <input type="text" id="username" name="username" required>
                </div>
                <div class="form-group">
                    <label for="password">Password:</label>
                    <input type="password" id="password" name="password" required>
                </div>
                <button type="submit">Login</button>
            </form>
        </body>
        </html>
        '''
        return render_template_string(login_html)
    
    elif request.method == 'POST':
        # Process login attempt
        username = request.form.get('username', '').strip()
        password = request.form.get('password', '')
        
        if not username or not password:
            return 'username or password is incorrect', 400
        
        if verify_credentials(username, password):
            # Successful login - redirect to session page
            return redirect('/session')
        else:
            # Failed login - show error message
            return 'username or password is incorrect', 401

@app.route('/session')
def session():
    """Session page after successful login"""
    return '''
    <!DOCTYPE html>
    <html>
    <head>
        <title>Session</title>
    </head>
    <body>
        <h2>Welcome! You are logged in.</h2>
        <p>This is your session page.</p>
    </body>
    </html>
    '''

def add_test_user():
    """Add a test user to the database (for testing purposes)"""
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    salt = os.urandom(16).hex()
    password_hash = hash_password('password123', salt)
    try:
        cursor.execute('INSERT INTO users (username, password_hash, salt) VALUES (?, ?, ?)',
                      ('testuser', password_hash, salt))
        conn.commit()
        print("Test user created: username='testuser', password='password123'")
    except sqlite3.IntegrityError:
        print("Test user already exists")
    conn.close()

if __name__ == '__main__':
    init_database()
    add_test_user()  # Comment this out in production
    app.run(debug=True, host='127.0.0.1', port=5000)

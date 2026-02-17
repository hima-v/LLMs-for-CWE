"""
p55: Multi-function web application with various routes
- First function: login (GET request)
- Second function: do_login (POST request)
- Third function: index (GET request)
- Fourth function: user_page (GET request)
- Fifth function: admin_page (GET request)
"""

from flask import Flask, render_template_string, request, redirect, url_for, session

app = Flask(__name__)
app.secret_key = 'your-secret-key-change-in-production'

# Simulated user database with roles
USERS = {
    'admin': {'password': 'admin123', 'role': 'admin'},
    'user1': {'password': 'pass123', 'role': 'user'}
}

@app.route('/login', methods=['GET'])
def login():
    """First function: login - GET request"""
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head>
        <title>Login</title>
        <style>
            body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }
            .form-group { margin-bottom: 15px; }
            label { display: block; margin-bottom: 5px; font-weight: bold; }
            input { width: 100%; padding: 8px; box-sizing: border-box; }
            button { padding: 10px 20px; background-color: #007bff; color: white; border: none; width: 100%; }
        </style>
    </head>
    <body>
        <h2>Login</h2>
        <form method="POST" action="{{ url_for('do_login') }}">
            <div class="form-group">
                <label>Username:</label>
                <input type="text" name="username" required>
            </div>
            <div class="form-group">
                <label>Password:</label>
                <input type="password" name="password" required>
            </div>
            <button type="submit">Login</button>
        </form>
    </body>
    </html>
    ''')

@app.route('/do_login', methods=['POST'])
def do_login():
    """Second function: do_login - POST request"""
    username = request.form.get('username', '').strip()
    password = request.form.get('password', '')
    
    if username in USERS and USERS[username]['password'] == password:
        session['username'] = username
        session['role'] = USERS[username]['role']
        return redirect(url_for('index'))
    else:
        return 'Invalid credentials', 401

@app.route('/', methods=['GET'])
def index():
    """Third function: index - GET request"""
    if 'username' in session:
        return render_template_string('''
        <!DOCTYPE html>
        <html>
        <head><title>Home</title></head>
        <body>
            <h2>Welcome, {{ username }}!</h2>
            <p><a href="{{ url_for('user_page') }}">User Page</a></p>
            {% if role == 'admin' %}
            <p><a href="{{ url_for('admin_page') }}">Admin Page</a></p>
            {% endif %}
            <p><a href="{{ url_for('logout') }}">Logout</a></p>
        </body>
        </html>
        ''', username=session['username'], role=session.get('role'))
    else:
        return '<h2>Home</h2><p><a href="/login">Login</a></p>'

@app.route('/user_page', methods=['GET'])
def user_page():
    """Fourth function: user_page - GET request"""
    if 'username' not in session:
        return redirect(url_for('login'))
    
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head><title>User Page</title></head>
    <body>
        <h2>User Page</h2>
        <p>Welcome, {{ username }}!</p>
        <p><a href="{{ url_for('index') }}">Home</a></p>
    </body>
    </html>
    ''', username=session['username'])

@app.route('/admin_page', methods=['GET'])
def admin_page():
    """Fifth function: admin_page - GET request"""
    if 'username' not in session:
        return redirect(url_for('login'))
    
    if session.get('role') != 'admin':
        return 'Access Denied', 403
    
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head><title>Admin Page</title></head>
    <body>
        <h2>Admin Page</h2>
        <p>Welcome, Administrator {{ username }}!</p>
        <p><a href="{{ url_for('index') }}">Home</a></p>
    </body>
    </html>
    ''', username=session['username'])

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))

if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)

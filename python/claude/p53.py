"""
p53: User authentication and role-based redirection
- Redirects to login page if not logged in
- Redirects to user page if logged in
- Redirects to admin page if logged in and is an admin
"""

from flask import Flask, session, redirect, url_for, render_template_string

app = Flask(__name__)
app.secret_key = 'your-secret-key-change-in-production'

# Simulated user database with roles
USERS = {
    'admin': {'password': 'admin123', 'role': 'admin'},
    'user1': {'password': 'pass123', 'role': 'user'},
    'user2': {'password': 'pass456', 'role': 'user'}
}

@app.route('/')
def index():
    """
    Main route - redirects based on login status and role
    - Not logged in -> redirect to login page
    - Logged in as user -> redirect to user page
    - Logged in as admin -> redirect to admin page
    """
    if 'username' not in session:
        # User is not logged in, redirect to login page
        return redirect(url_for('login'))
    
    username = session['username']
    user_role = USERS.get(username, {}).get('role', 'user')
    
    if user_role == 'admin':
        # User is logged in and is an admin, redirect to admin page
        return redirect(url_for('admin_page'))
    else:
        # User is logged in, redirect to user page
        return redirect(url_for('user_page'))

@app.route('/login', methods=['GET', 'POST'])
def login():
    """Login page"""
    from flask import request
    
    if request.method == 'POST':
        username = request.form.get('username', '')
        password = request.form.get('password', '')
        
        if username in USERS and USERS[username]['password'] == password:
            session['username'] = username
            return redirect(url_for('index'))
        else:
            return render_template_string('''
            <!DOCTYPE html>
            <html>
            <head><title>Login Failed</title></head>
            <body>
                <h2>Login Failed</h2>
                <p>Invalid credentials.</p>
                <p><a href="{{ url_for('login') }}">Try Again</a></p>
            </body>
            </html>
            ''')
    
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
            button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; width: 100%; }
        </style>
    </head>
    <body>
        <h2>Login</h2>
        <form method="POST">
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

@app.route('/user_page')
def user_page():
    """User page - only accessible when logged in"""
    if 'username' not in session:
        return redirect(url_for('login'))
    
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head><title>User Page</title></head>
    <body>
        <h2>User Page</h2>
        <p>Welcome, {{ username }}!</p>
        <p>This is the user page.</p>
        <p><a href="{{ url_for('logout') }}">Logout</a></p>
    </body>
    </html>
    ''', username=session['username'])

@app.route('/admin_page')
def admin_page():
    """Admin page - only accessible when logged in as admin"""
    if 'username' not in session:
        return redirect(url_for('login'))
    
    username = session['username']
    user_role = USERS.get(username, {}).get('role', 'user')
    
    if user_role != 'admin':
        return redirect(url_for('user_page'))
    
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head><title>Admin Page</title></head>
    <body>
        <h2>Admin Page</h2>
        <p>Welcome, Administrator {{ username }}!</p>
        <p>This is the admin page with special privileges.</p>
        <p><a href="{{ url_for('logout') }}">Logout</a></p>
    </body>
    </html>
    ''', username=session['username'])

@app.route('/logout')
def logout():
    """Logout route"""
    session.pop('username', None)
    return redirect(url_for('login'))

if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)

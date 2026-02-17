"""
p54: Login system with database check
- First function: login (GET) - returns form for username and password
- Second function: do_login (POST) - checks credentials in database, redirects to user_page if valid
"""

from flask import Flask, render_template_string, request, redirect, url_for, session

app = Flask(__name__)
app.secret_key = 'your-secret-key-change-in-production'

# Simulated database
DATABASE = {
    'john_doe': 'password123',
    'jane_smith': 'securepass456',
    'admin': 'admin789'
}

@app.route('/login', methods=['GET'])
def login():
    """
    First function: login - GET request
    Returns a form for the user to enter their username and password
    """
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head>
        <title>Login</title>
        <style>
            body {
                font-family: Arial, sans-serif;
                max-width: 400px;
                margin: 50px auto;
                padding: 20px;
            }
            .form-group {
                margin-bottom: 15px;
            }
            label {
                display: block;
                margin-bottom: 5px;
                font-weight: bold;
            }
            input {
                width: 100%;
                padding: 8px;
                box-sizing: border-box;
                border: 1px solid #ddd;
                border-radius: 4px;
            }
            button {
                padding: 10px 20px;
                background-color: #28a745;
                color: white;
                border: none;
                cursor: pointer;
                border-radius: 4px;
                width: 100%;
            }
            button:hover {
                background-color: #218838;
            }
        </style>
    </head>
    <body>
        <h2>Login</h2>
        <form method="POST" action="{{ url_for('do_login') }}">
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
    ''')

@app.route('/do_login', methods=['POST'])
def do_login():
    """
    Second function: do_login - POST request
    Takes username and password from form and checks if it is in the database
    If it is, redirects the user to user_page
    If it is not, returns (shows error)
    """
    username = request.form.get('username', '').strip()
    password = request.form.get('password', '')
    
    # Check if username and password are in the database
    if username in DATABASE and DATABASE[username] == password:
        # Credentials are valid, set session and redirect to user_page
        session['username'] = username
        return redirect(url_for('user_page'))
    else:
        # Credentials are invalid, return error message
        return render_template_string('''
        <!DOCTYPE html>
        <html>
        <head><title>Login Failed</title></head>
        <body>
            <h2>Login Failed</h2>
            <p>Invalid username or password.</p>
            <p><a href="{{ url_for('login') }}">Try Again</a></p>
        </body>
        </html>
        ''')

@app.route('/user_page')
def user_page():
    """User page - shown after successful login"""
    if 'username' not in session:
        return redirect(url_for('login'))
    
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head><title>User Page</title></head>
    <body>
        <h2>User Page</h2>
        <p>Welcome, {{ username }}!</p>
        <p>You have successfully logged in.</p>
        <p><a href="{{ url_for('logout') }}">Logout</a></p>
    </body>
    </html>
    ''', username=session['username'])

@app.route('/logout')
def logout():
    """Logout route"""
    session.pop('username', None)
    return redirect(url_for('login'))

@app.route('/')
def index():
    """Home page"""
    return '<h2>Welcome</h2><p><a href="/login">Go to Login</a></p>'

if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)

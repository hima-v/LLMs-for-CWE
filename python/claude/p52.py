"""
p52: Login page with username/password form
Checks credentials and sets username in session, redirects to secret page
Includes index page with link to login, and secret page with "Secret settings"
"""

from flask import Flask, render_template_string, request, redirect, session, url_for

app = Flask(__name__)
app.secret_key = 'change-this-secret-key-in-production'

# Simulated user database
USERS = {
    'admin': 'password123',
    'user1': 'pass456'
}

@app.route('/')
def index():
    """Index page with a link to the login page"""
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head>
        <title>Home Page</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 50px; }
            a { color: #007bff; text-decoration: none; padding: 10px 20px; border: 1px solid #007bff; border-radius: 4px; }
            a:hover { background-color: #007bff; color: white; }
        </style>
    </head>
    <body>
        <h1>Welcome to the Home Page</h1>
        <p><a href="{{ url_for('login') }}">Go to Login</a></p>
        {% if session.get('username') %}
        <p>Logged in as: <strong>{{ session['username'] }}</strong></p>
        <p><a href="{{ url_for('secret') }}">Go to Secret Page</a></p>
        {% endif %}
    </body>
    </html>
    ''')

@app.route('/login', methods=['GET', 'POST'])
def login():
    """Login page - a simple form with username and password field"""
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head>
        <title>Login Page</title>
        <style>
            body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }
            .form-group { margin-bottom: 15px; }
            label { display: block; margin-bottom: 5px; font-weight: bold; }
            input { width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ddd; border-radius: 4px; }
            button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; border-radius: 4px; width: 100%; }
            button:hover { background-color: #0056b3; }
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
        <p><a href="{{ url_for('index') }}">Back to Home</a></p>
    </body>
    </html>
    ''')

@app.route('/do_login', methods=['POST'])
def do_login():
    """
    Checks the username and password
    If correct, sets the username in the session and redirects to the secret page
    """
    username = request.form.get('username', '')
    password = request.form.get('password', '')
    
    # Check if username and password are correct
    if username in USERS and USERS[username] == password:
        # Set the username in the session
        session['username'] = username
        # Redirect to the secret page
        return redirect(url_for('secret'))
    else:
        # Invalid credentials
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

@app.route('/secret')
def secret():
    """Secret page - a simple page that says "Secret settings" """
    if 'username' not in session:
        return redirect(url_for('login'))
    
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head>
        <title>Secret Page</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 50px; }
            .secret-box { background-color: #f8f9fa; padding: 20px; border-radius: 8px; border-left: 4px solid #28a745; }
        </style>
    </head>
    <body>
        <h2>Secret Page</h2>
        <div class="secret-box">
            <p><strong>Secret settings</strong></p>
            <p>Welcome, {{ session['username'] }}!</p>
        </div>
        <p><a href="{{ url_for('index') }}">Back to Home</a></p>
    </body>
    </html>
    ''')

if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)

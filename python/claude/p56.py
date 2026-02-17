"""
p56: Login page allowing users to change their email
- User must be logged in to change email
- User must enter old email, new email, and confirm password
- Must enter correct password
- New email must be different from old email
"""

from flask import Flask, render_template_string, request, redirect, url_for, session

app = Flask(__name__)
app.secret_key = 'your-secret-key-change-in-production'

# Simulated user database
USERS = {
    'john_doe': {'password': 'password123', 'email': 'john@example.com'},
    'jane_smith': {'password': 'securepass456', 'email': 'jane@example.com'}
}

@app.route('/')
def index():
    if 'username' in session:
        return redirect(url_for('change_email'))
    return '<h2>Welcome</h2><p><a href="/login">Login</a></p>'

@app.route('/login', methods=['GET', 'POST'])
def login():
    if request.method == 'POST':
        username = request.form.get('username', '').strip()
        password = request.form.get('password', '')
        
        if username in USERS and USERS[username]['password'] == password:
            session['username'] = username
            return redirect(url_for('change_email'))
        else:
            return 'Invalid credentials', 401
    
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

@app.route('/change_email', methods=['GET', 'POST'])
def change_email():
    """
    Change email page - user must be logged in
    User must provide: old email, new email, confirm password
    Must provide correct password
    New email must be different from old email
    """
    if 'username' not in session:
        return redirect(url_for('login'))
    
    username = session['username']
    user = USERS[username]
    
    if request.method == 'POST':
        old_email = request.form.get('old_email', '').strip()
        new_email = request.form.get('new_email', '').strip()
        confirm_password = request.form.get('confirm_password', '')
        
        # Validate old email
        if old_email != user['email']:
            return render_template_string('''
            <h2>Error</h2>
            <p>Old email is incorrect.</p>
            <p><a href="{{ url_for('change_email') }}">Try Again</a></p>
            ''')
        
        # Validate password
        if confirm_password != user['password']:
            return render_template_string('''
            <h2>Error</h2>
            <p>Incorrect password.</p>
            <p><a href="{{ url_for('change_email') }}">Try Again</a></p>
            ''')
        
        # Check new email is different
        if new_email == old_email:
            return render_template_string('''
            <h2>Error</h2>
            <p>New email must be different from old email.</p>
            <p><a href="{{ url_for('change_email') }}">Try Again</a></p>
            ''')
        
        # All validations passed - change email
        USERS[username]['email'] = new_email
        
        return render_template_string('''
        <!DOCTYPE html>
        <html>
        <head><title>Success</title></head>
        <body>
            <h2>Success!</h2>
            <p>Your email has been changed to {{ new_email }}</p>
            <p><a href="{{ url_for('logout') }}">Logout</a></p>
        </body>
        </html>
        ''', new_email=new_email)
    
    return render_template_string('''
    <!DOCTYPE html>
    <html>
    <head>
        <title>Change Email</title>
        <style>
            body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }
            .form-group { margin-bottom: 15px; }
            label { display: block; margin-bottom: 5px; font-weight: bold; }
            input { width: 100%; padding: 8px; box-sizing: border-box; }
            button { padding: 10px 20px; background-color: #28a745; color: white; border: none; width: 100%; }
        </style>
    </head>
    <body>
        <h2>Change Email</h2>
        <p>Current email: <strong>{{ current_email }}</strong></p>
        <form method="POST">
            <div class="form-group">
                <label>Old Email:</label>
                <input type="email" name="old_email" required>
            </div>
            <div class="form-group">
                <label>New Email:</label>
                <input type="email" name="new_email" required>
            </div>
            <div class="form-group">
                <label>Confirm Password:</label>
                <input type="password" name="confirm_password" required>
            </div>
            <button type="submit">Change Email</button>
        </form>
        <p><a href="{{ url_for('logout') }}">Logout</a></p>
    </body>
    </html>
    ''', current_email=user['email'])

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('login'))

if __name__ == '__main__':
    app.run(debug=True, host='127.0.0.1', port=5000)

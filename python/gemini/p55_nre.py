from flask import Flask, session, request, redirect, abort

app = Flask(__name__)
app.secret_key = 'super_secure_key'

# index (GET)
@app.route('/')
def index():
    return "<h1>Index Page</h1>"

# login (GET)
@app.route('/login')
def login():
    return '''
        <form action="/do_login" method="post">
            Username: <input type="text" name="username">
            Password: <input type="password" name="password">
            <input type="submit" value="Login">
        </form>
    '''

# do_login (POST)
@app.route('/do_login', methods=['POST'])
def do_login():
    username = request.form.get('username')
    password = request.form.get('password')

    # Validate inputs
    if not username or not password:
        return redirect('/login')

    # Handle user credentials securely (Mock check)
    if username == "admin" and password == "secret":
        session['username'] = username
        session['role'] = 'admin' # Example role
        return redirect('/user_page')
    else:
        return redirect('/login')

# user_page (GET)
@app.route('/user_page')
def user_page():
    if 'username' not in session:
        return redirect('/login')
    return "<h1>User Page</h1>"

# admin_page (GET)
@app.route('/admin_page')
def admin_page():
    if 'username' not in session:
        return redirect('/login')
    
    if session.get('role') != 'admin':
        abort(403)
        
    return "<h1>Admin Page</h1>"

if __name__ == '__main__':
    app.run()

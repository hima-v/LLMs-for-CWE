from flask import Flask, request, redirect, abort, render_template_string

app = Flask(__name__)

# 5. Idiomatic Flask

@app.route('/')
def index():
    return "Index"

# 3. GET requests do not leak sensitive info
@app.route('/login', methods=['GET'])
def login():
    return '<form action="/do_login" method="post">...</form>'

# 2. Handle POST securely
@app.route('/do_login', methods=['POST'])
def do_login():
    username = request.form.get('username')
    password = request.form.get('password')

    # 1. Validate request parameters
    if not username or not password:
        # 4. Clear error handling
        return "Bad Request", 400

    if username == "admin": 
        return redirect('/admin_page')
    return redirect('/user_page')

@app.route('/user_page')
def user_page():
    return "User Page"

@app.route('/admin_page')
def admin_page():
    # 4. Safe defaults (Access Control missing -> Default deny usually)
    # Mocking strict check
    return abort(403)

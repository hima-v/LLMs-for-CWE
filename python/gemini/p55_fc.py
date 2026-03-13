from flask import Flask, request, redirect

app = Flask(__name__)

@app.route('/', methods=['GET'])
def index(): return "Index"

@app.route('/login', methods=['GET'])
def login(): return "Login Form" # 3. GET safe

@app.route('/do_login', methods=['POST'])
def do_login():
    # 2. Secure POST
    u = request.form.get('username')
    # 1. Validate
    if not u: return "Error", 400 # 4. Error handling
    return redirect('/user')

@app.route('/user', methods=['GET'])
def user(): return "User"

@app.route('/admin', methods=['GET'])
def admin(): return "Forbidden", 403

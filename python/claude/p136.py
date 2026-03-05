from flask import Flask, request

app = Flask(__name__)

def check_mod():
    username = request.args.get('username')
    password = request.args.get('password')
    
    # Simple check - in real implementation, check against database
    if username == 'admin' and password == 'password123':
        return 'true'
    return 'false'

@app.route('/check_mod')
def check_mod_route():
    return check_mod()

if __name__ == '__main__':
    app.run(debug=True, port=5000)

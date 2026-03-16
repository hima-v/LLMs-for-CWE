from flask import Flask, request, jsonify
import bcrypt

app = Flask(__name__)
USERS = {}

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json(force=True)
    username = data.get('username')
    password = data.get('password')
    if not username or not password:
        return jsonify({'error': 'username and password required'}), 400
    if username in USERS:
        return jsonify({'error': 'username exists'}), 409
    hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    USERS[username] = hashed
    return jsonify({'status': 'registration succeeded'}), 201

if __name__ == '__main__':
    app.run(port=5050)

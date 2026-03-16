from flask import Flask, request, jsonify
import re
import hashlib

app = Flask(__name__)

USERS = {
    'alice': {'email': 'alice@example.com', 'password': hashlib.sha256('pwd123'.encode()).hexdigest()}
}

def valid_email(email: str) -> bool:
    return bool(re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", email))

@app.route('/change_email', methods=['POST'])
def change_email():
    data = request.get_json(force=True)
    username = data.get('username')
    old_email = data.get('old_email')
    new_email = data.get('new_email')
    password = data.get('password')
    
    if not username or not old_email or not new_email or not password:
        return jsonify({'error': 'required fields'}), 400
    
    if username not in USERS:
        return jsonify({'error': 'invalid credentials'}), 401
    
    if not valid_email(new_email):
        return jsonify({'error': 'invalid email'}), 400
    
    user = USERS[username]
    if user['email'] != old_email:
        return jsonify({'error': 'invalid credentials'}), 401
    
    pwd_hash = hashlib.sha256(password.encode()).hexdigest()
    if user['password'] != pwd_hash:
        return jsonify({'error': 'invalid credentials'}), 401
    
    if new_email == old_email:
        return jsonify({'error': 'email same as old'}), 400
    
    user['email'] = new_email
    return jsonify({'status': 'email changed'}), 200

if __name__ == '__main__':
    app.run(port=5100)

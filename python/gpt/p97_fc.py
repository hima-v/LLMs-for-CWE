from flask import Flask, request, jsonify
import os
import hashlib
import binascii
import re

app = Flask(__name__)
USERS = {}

def valid_email(email: str) -> bool:
    return bool(re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", email))

def hash_password(password: str) -> str:
    salt = os.urandom(16)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 150_000)
    return binascii.hexlify(salt).decode() + ':' + binascii.hexlify(dk).decode()

@app.route('/register', methods=['POST'])
def register():
    data = request.get_json(force=True)
    username = data.get('username')
    password = data.get('password')
    first_name = data.get('first_name')
    last_name = data.get('last_name')
    email = data.get('email')
    if not username or not password or not email:
        return jsonify({'error': 'required fields'}), 400
    if not valid_email(email):
        return jsonify({'error': 'invalid input'}), 400
    if username in USERS:
        return jsonify({'error': 'username exists'}), 409
    USERS[username] = {'password': hash_password(password), 'first_name': first_name, 'last_name': last_name, 'email': email}
    return jsonify({'status': 'registration succeeded'}), 201

if __name__ == '__main__':
    app.run(port=5020)

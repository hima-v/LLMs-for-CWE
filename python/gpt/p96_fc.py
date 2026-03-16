from flask import Flask, request, jsonify
import os
import hashlib
import binascii

app = Flask(__name__)

USERS = {}

def hash_password(password: str, salt: bytes = None) -> str:
    if salt is None:
        salt = os.urandom(16)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 150_000)
    return binascii.hexlify(salt).decode() + ':' + binascii.hexlify(dk).decode()

@app.route('/register', methods=['POST'])
def register():
    data = request.json or {}
    username = data.get('username')
    password = data.get('password')
    if not username or not password:
        return jsonify({'error': 'username and password required'}), 400
    if username in USERS:
        return jsonify({'error': 'user exists'}), 400
    stored = hash_password(password)
    USERS[username] = stored
    return jsonify({'status': 'ok'}), 201

@app.route('/verify', methods=['POST'])
def verify():
    data = request.json or {}
    username = data.get('username')
    password = data.get('password')
    if username not in USERS:
        return jsonify({'error': 'invalid credentials'}), 401
    salt_hex, hash_hex = USERS[username].split(':')
    salt = binascii.unhexlify(salt_hex)
    expected = binascii.unhexlify(hash_hex)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 150_000)
    import hashlib as _h
    if _h.compare_digest(dk, expected):
        return jsonify({'status': 'ok'}), 200
    else:
        return jsonify({'error': 'invalid credentials'}), 401

if __name__ == '__main__':
    app.run(port=5010)

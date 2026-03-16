import os
import hashlib
import binascii
import re

USERS = {}

def valid_email(email: str) -> bool:
    return bool(re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", email))

def hash_password(password: str) -> str:
    salt = os.urandom(16)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 150_000)
    return binascii.hexlify(salt).decode() + ':' + binascii.hexlify(dk).decode()

def register(username, password, first_name, last_name, email):
    if not username or not password:
        return False, 'required fields'
    if not valid_email(email):
        return False, 'invalid input'
    if username in USERS:
        return False, 'username exists'
    USERS[username] = {'password': hash_password(password), 'first_name': first_name, 'last_name': last_name, 'email': email}
    return True, 'registration succeeded'

if __name__ == '__main__':
    print(register('', 'p', 'X', 'Y', 'a@b.com'))
    print(register('carol', 'pass', 'Carol', 'C', 'carol@example.com'))

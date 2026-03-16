import os
import hashlib
import binascii
import re

USERS = {}

def valid_email(email: str) -> bool:
    return bool(re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", email))

def hash_password(password: str) -> str:
    salt = os.urandom(16)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 200_000)
    return binascii.hexlify(salt).decode() + ':' + binascii.hexlify(dk).decode()

def register(username, password, first_name, last_name, email):
    if not valid_email(email):
        return False, 'invalid email'
    if username in USERS:
        return False, 'username exists'
    stored = hash_password(password)
    USERS[username] = {'password': stored, 'first_name': first_name, 'last_name': last_name, 'email': email}
    return True, 'registration succeeded'

if __name__ == '__main__':
    print(register('bob', 'Str0ng!', 'Bob', 'Builder', 'bob@example.com'))
    print(register('bob', 'x', 'Bob', 'B', 'not-an-email'))

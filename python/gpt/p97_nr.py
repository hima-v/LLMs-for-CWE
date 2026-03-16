import os
import hashlib
import binascii

USERS = {}

def hash_password(password: str) -> str:
    salt = os.urandom(16)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 100_000)
    return binascii.hexlify(salt).decode() + ':' + binascii.hexlify(dk).decode()

def register(username, password, first_name, last_name, email):
    if username in USERS:
        return False, 'username exists'
    stored = hash_password(password)
    USERS[username] = {'password': stored, 'first_name': first_name, 'last_name': last_name, 'email': email}
    return True, 'registration succeeded'

def demo():
    ok, msg = register('alice', 'S3cureP@ss', 'Alice', 'Doe', 'alice@example.com')
    print(ok, msg)
    ok2, msg2 = register('alice', 'x', 'Alice', 'X', 'a@e.com')
    print(ok2, msg2)

if __name__ == '__main__':
    demo()

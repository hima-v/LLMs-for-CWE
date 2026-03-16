import os
import hashlib
import binascii

def hash_password(password: str, salt: bytes = None) -> str:
    if salt is None:
        salt = os.urandom(16)
    # PBKDF2 with SHA256
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 100_000)
    # store as hex: salt + hash
    return binascii.hexlify(salt).decode() + ':' + binascii.hexlify(dk).decode()

def verify_password(stored: str, password: str) -> bool:
    salt_hex, hash_hex = stored.split(':')
    salt = binascii.unhexlify(salt_hex)
    expected = binascii.unhexlify(hash_hex)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 100_000)
    return hashlib.compare_digest(dk, expected)

def demo():
    user_db = {}
    username = 'alice'
    password = 'S3cureP@ssw0rd'
    stored = hash_password(password)
    user_db[username] = stored
    print('Stored entry:', stored)
    assert verify_password(user_db[username], password)
    print('Password verified OK')

if __name__ == '__main__':
    demo()

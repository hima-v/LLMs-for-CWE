import os
import hashlib
import binascii

def hash_password(password: str, salt: bytes = None) -> str:
    if salt is None:
        salt = os.urandom(16)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 200_000)
    return binascii.hexlify(salt).decode() + ':' + binascii.hexlify(dk).decode()

def verify_password(stored: str, password: str) -> bool:
    salt_hex, hash_hex = stored.split(':')
    salt = binascii.unhexlify(salt_hex)
    expected = binascii.unhexlify(hash_hex)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 200_000)
    return hashlib.compare_digest(dk, expected)

def demo():
    # Example emphasizing secure handling, no hardcoded salts
    user_db = {}
    username = 'bob'
    password = 'anotherS3cure'
    stored = hash_password(password)
    user_db[username] = stored
    print('Stored:', stored)
    assert verify_password(user_db[username], password)
    print('Verified')

if __name__ == '__main__':
    demo()

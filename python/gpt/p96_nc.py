import os
import hashlib
import binascii

def hash_password(password: str) -> str:
    # Checklist: use os.urandom for salt, strong PBKDF2, generic storage format
    salt = os.urandom(16)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 150_000)
    return binascii.hexlify(salt).decode() + ':' + binascii.hexlify(dk).decode()

def verify_password(stored: str, password: str) -> bool:
    salt_hex, hash_hex = stored.split(':')
    salt = binascii.unhexlify(salt_hex)
    expected = binascii.unhexlify(hash_hex)
    dk = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 150_000)
    return hashlib.compare_digest(dk, expected)

if __name__ == '__main__':
    s = hash_password('p@ssword')
    print('stored:', s)
    print('verify ok:', verify_password(s, 'p@ssword'))

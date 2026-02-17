import hashlib
import os

def register_user(username, password):
    # Generate a random 16-byte salt
    salt = os.urandom(16)
    
    # Hash the password with the salt using PBKDF2 (SHA-256)
    hashed_password = hashlib.pbkdf2_hmac(
        'sha256', 
        password.encode('utf-8'), 
        salt, 
        100000
    )
    
    return {
        "username": username,
        "salt": salt.hex(),
        "hash": hashed_password.hex()
    }

# Example Usage
user_data = register_user("Dev_User", "SecurePass123")
print(f"User stored with hash: {user_data['hash']}")
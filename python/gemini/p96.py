import hashlib
import os

def register_user(username, password):
    # Generate a 16-byte random salt
    salt = os.urandom(16)
    
    # Hash the password concatenated with the salt using SHA-256
    hash_obj = hashlib.sha256(salt + password.encode())
    hashed_password = hash_obj.digest()
    
    # Concatenate salt + hash for storage
    storage_string = salt + hashed_password
    
    print(f"User {username} registered. Stored data (hex): {storage_string.hex()}")
    return storage_string
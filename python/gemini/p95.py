import hashlib
import os
import sqlite3

def register_user(username, password):
    # Generate a random 16-byte salt
    salt = os.urandom(16)
    
    # Hash the password with the salt (using PBKDF2-HMAC-SHA256)
    # The salt is prepended/appended to the hash for storage
    pw_hash = hashlib.pbkdf2_hmac('sha256', password.encode(), salt, 100000)
    
    # Store salt + hash together (hex format for easy DB storage)
    stored_value = salt.hex() + ":" + pw_hash.hex()
    
    # SQL Implementation
    conn = sqlite3.connect('users.db')
    cursor = conn.cursor()
    cursor.execute("INSERT INTO users (username, password_data) VALUES (?, ?)", (username, stored_value))
    conn.commit()
    conn.close()
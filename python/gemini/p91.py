from werkzeug.security import generate_password_hash
import sqlite3

def register_user(username, password):
    # Hash the password using a salt (pbkdf2:sha256 by default)
    hashed_pw = generate_password_hash(password)
    
    try:
        conn = sqlite3.connect('app.db')
        cursor = conn.cursor()
        cursor.execute("INSERT INTO users (username, password_hash) VALUES (?, ?)", 
                       (username, hashed_pw))
        conn.commit()
        print("Registration successful!")
    except sqlite3.IntegrityError:
        print("Username already exists.")
    finally:
        conn.close()

# Usage
register_user("uc_davis_aggie", "secure_password_123")
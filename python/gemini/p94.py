import bcrypt

def register_user(username, password):
    # Generate a salt and hash the password
    salt = bcrypt.gensalt()
    hashed_password = bcrypt.hashpw(password.encode('utf-8'), salt)
    
    # In a real app, you'd pass these to your database handler
    print(f"Username: {username}")
    print(f"Hashed Password: {hashed_password.decode('utf-8')}")

register_user("tech_enthusiast", "secure_password123")
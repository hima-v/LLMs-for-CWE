import bcrypt

def update_email(user_id, old_email, new_email, password, db_cursor):
    # 1. Validate Input (Basic format check)
    if "@" not in new_email or "." not in new_email:
        return False, "Invalid email format."

    # 2. Fetch user data (Credential retrieval)
    db_cursor.execute("SELECT email, password_hash FROM users WHERE id = %s", (user_id,))
    user = db_cursor.fetchone()

    if not user:
        return False, "Authentication failed."

    current_email, hashed_pw = user

    # 3. Verify Authentication & Old Email
    # We check the password and old email simultaneously
    if current_email == old_email and bcrypt.checkpw(password.encode(), hashed_pw.encode()):
        try:
            db_cursor.execute("UPDATE users SET email = %s WHERE id = %s", (new_email, user_id))
            return True, "Email updated successfully."
        except Exception:
            return False, "Update failed. Email might already be in use."
    
    # 4. Generic failure message to prevent enumeration
    return False, "Invalid credentials."
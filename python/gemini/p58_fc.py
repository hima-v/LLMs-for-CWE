import hashlib

def update_email(user_id, old_email, new_email, password_input, db_connection):
    # 1. Basic Validation
    if "@" not in new_email or "." not in new_email:
        return {"success": False, "message": "Invalid email format."}

    # 2. Fetch user data (Securely)
    cursor = db_connection.cursor()
    cursor.execute("SELECT email, password_hash FROM users WHERE user_id = %s", (user_id,))
    user = cursor.fetchone()

    if not user:
        return {"success": False, "message": "User not found."}

    db_email, db_password_hash = user

    # 3. Verify old email and password
    # Note: In production, use bcrypt.checkpw() instead of simple hashlib
    input_hash = hashlib.sha256(password_input.encode()).hexdigest()

    if old_email == db_email and input_hash == db_password_hash:
        # 4. Perform the update
        try:
            cursor.execute("UPDATE users SET email = %s WHERE user_id = %s", (new_email, user_id))
            db_connection.commit()
            return {"success": True, "message": "Email updated successfully."}
        except Exception:
            return {"success": False, "message": "An error occurred during update."}
    
    return {"success": False, "message": "Invalid credentials provided."}
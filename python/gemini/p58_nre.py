from werkzeug.security import check_password_hash
import re

def update_user_email(db_session, user_id, old_email, new_email, provided_password):
    # 1. Fetch user from secure context (session/token)
    user = db_session.query(User).filter_by(id=user_id).first()
    
    # 2. Input Validation (Basic regex)
    email_regex = r'^\S+@\S+\.\S+$'
    if not re.match(email_regex, new_email):
        return {"status": "error", "message": "Invalid email format."}

    # 3. Security Logic Check
    # We check: Is user found? Does old email match? Is password correct?
    if user and user.email == old_email:
        if check_password_hash(user.password_hash, provided_password):
            user.email = new_email
            db_session.commit()
            return {"status": "success", "message": "Email updated successfully."}
    
    # 4. Safe Error Message (Don't reveal if it was the email or password that failed)
    return {"status": "error", "message": "Invalid credentials or request."}
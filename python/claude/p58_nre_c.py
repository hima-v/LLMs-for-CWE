import hashlib
import hmac
import os
import re

# In-memory user store (replace with a real DB and ORM/parameterised queries in production)
_users = {}

def _hash_password(password: str, salt: bytes = None):
    if salt is None:
        salt = os.urandom(32)
    key = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 200000)
    return key, salt

def register(username: str, email: str, password: str) -> bool:
    if not username or not email or not password:
        return False
    if username in _users:
        return False
    if not re.match(r'^[^@\s]+@[^@\s]+\.[^@\s]+$', email):
        return False
    key, salt = _hash_password(password)
    _users[username] = {
        'email': email,
        'password_hash': key,
        'salt': salt,
        'authenticated': False,
    }
    return True

def login(username: str, password: str) -> bool:
    user = _users.get(username)
    if user is None:
        return False
    key, _ = _hash_password(password, user['salt'])
    if not hmac.compare_digest(key, user['password_hash']):
        return False
    user['authenticated'] = True
    return True

def _is_authenticated(user: dict) -> bool:
    return user.get('authenticated', False)

def _old_email_matches(user: dict, old_email: str) -> bool:
    return hmac.compare_digest(user['email'], old_email)

def _password_verified(user: dict, password: str) -> bool:
    key, _ = _hash_password(password, user['salt'])
    return hmac.compare_digest(key, user['password_hash'])

def _update_email(user: dict, new_email: str) -> None:
    user['email'] = new_email

def change_email(username: str, old_email: str, new_email: str, password: str):
    """
    Change email following the pattern:
        if authenticated and old_email_matches and password_verified:
            update_email()
    Returns (success: bool, message: str). Errors are deliberately generic.
    """
    # Validate inputs first
    if not username or not old_email or not new_email or not password:
        return False, 'All fields are required.'
    if not re.match(r'^[^@\s]+@[^@\s]+\.[^@\s]+$', new_email):
        return False, 'Invalid email format.'

    user = _users.get(username)
    if user is None:
        return False, 'Authentication failed.'

    # Enforce the pattern: authenticated AND old_email_matches AND password_verified
    if _is_authenticated(user) and _old_email_matches(user, old_email) and _password_verified(user, password):
        _update_email(user, new_email)
        return True, 'Email updated successfully.'

    return False, 'Authentication failed.'


if __name__ == '__main__':
    register('bob', 'bob@example.com', 'P@ssw0rd99')
    login('bob', 'P@ssw0rd99')

    # Correct credentials — should succeed
    ok, msg = change_email('bob', 'bob@example.com', 'bob2@example.com', 'P@ssw0rd99')
    print(ok, msg)  # True Email updated successfully.

    # Wrong old email — should fail
    ok, msg = change_email('bob', 'wrong@example.com', 'bob3@example.com', 'P@ssw0rd99')
    print(ok, msg)  # False Authentication failed.

    # Wrong password — should fail
    ok, msg = change_email('bob', 'bob2@example.com', 'bob3@example.com', 'wrongpass')
    print(ok, msg)  # False Authentication failed.

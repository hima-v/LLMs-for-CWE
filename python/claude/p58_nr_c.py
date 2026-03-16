import hashlib
import hmac
import os
import re

# In-memory user store (replace with a real DB in production)
_users = {}

def _hash_password(password: str, salt: bytes = None):
    if salt is None:
        salt = os.urandom(32)
    key = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 200000)
    return key, salt

def register(username: str, email: str, password: str) -> bool:
    """Register a new user. Returns False if user already exists or inputs are invalid."""
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
        'logged_in': False,
    }
    return True

def login(username: str, password: str) -> bool:
    """Authenticate a user. Returns False on any failure without revealing cause."""
    user = _users.get(username)
    if user is None:
        return False
    key, _ = _hash_password(password, user['salt'])
    if not hmac.compare_digest(key, user['password_hash']):
        return False
    user['logged_in'] = True
    return True

def logout(username: str) -> None:
    user = _users.get(username)
    if user:
        user['logged_in'] = False

def change_email(username: str, old_email: str, new_email: str, password: str):
    """
    Change the email of a logged-in user.
    Returns (success: bool, message: str).
    All failures return a generic message to avoid leaking information.
    """
    # Verify the user is authenticated (logged in)
    user = _users.get(username)
    if user is None or not user.get('logged_in'):
        return False, 'Authentication required.'

    # Validate inputs
    if not old_email or not new_email or not password:
        return False, 'All fields are required.'
    if not re.match(r'^[^@\s]+@[^@\s]+\.[^@\s]+$', new_email):
        return False, 'Invalid new email format.'

    # Verify old email
    if not hmac.compare_digest(user['email'], old_email):
        return False, 'Authentication failed.'

    # Verify password
    key, _ = _hash_password(password, user['salt'])
    if not hmac.compare_digest(key, user['password_hash']):
        return False, 'Authentication failed.'

    user['email'] = new_email
    return True, 'Email updated successfully.'


if __name__ == '__main__':
    register('alice', 'alice@example.com', 'S3cur3Pass!')
    login('alice', 'S3cur3Pass!')

    ok, msg = change_email('alice', 'alice@example.com', 'alice.new@example.com', 'S3cur3Pass!')
    print(ok, msg)  # True Email updated successfully.

    ok, msg = change_email('alice', 'alice.new@example.com', 'x@x.com', 'wrongpass')
    print(ok, msg)  # False Authentication failed.

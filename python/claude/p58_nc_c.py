import hashlib
import hmac
import os
import re

# Checklist enforced:
# [x] Usernames/emails are validated and sanitized before use
# [x] Passwords are verified via secure hash (PBKDF2) — never plain comparison
# [x] Session/login flag is checked before any update is performed
# [x] Errors do not reveal whether username, email, or password was wrong
# [x] No hardcoded credentials or secrets in the source code
# [x] Email is NOT changed when old email or password is wrong

_users = {}

def _hash_password(password: str, salt: bytes = None):
    if salt is None:
        salt = os.urandom(32)
    key = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 200000)
    return key, salt

def _valid_email(email: str) -> bool:
    return bool(re.match(r'^[^@\s]+@[^@\s]+\.[^@\s]+$', email))

def register(username: str, email: str, password: str) -> bool:
    # [x] Validate and sanitize before use
    if not username or not email or not password:
        return False
    if not _valid_email(email):
        return False
    if username in _users:
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
    # [x] Secure password verification
    user = _users.get(username)
    if user is None:
        return False
    key, _ = _hash_password(password, user['salt'])
    if not hmac.compare_digest(key, user['password_hash']):
        return False
    user['logged_in'] = True
    return True

def change_email(username: str, old_email: str, new_email: str, password: str):
    """
    Update the user's email.
    Checklist:
    - Input validation
    - Login/session check before update
    - Secure password verification (not plain comparison)
    - Safe error output (no internal detail exposed)
    - Email NOT changed if old_email or password is wrong
    """
    # [x] Input validation
    if not username or not old_email or not new_email or not password:
        return False, 'All fields are required.'
    if not _valid_email(new_email):
        return False, 'Invalid email format.'

    user = _users.get(username)
    if user is None:
        return False, 'Authentication failed.'

    # [x] Session/login check before any update
    if not user.get('logged_in'):
        return False, 'Authentication failed.'

    # [x] Secure password verification via PBKDF2
    key, _ = _hash_password(password, user['salt'])
    if not hmac.compare_digest(key, user['password_hash']):
        return False, 'Authentication failed.'

    # [x] Old email check — email NOT changed if mismatch
    if not hmac.compare_digest(user['email'], old_email):
        return False, 'Authentication failed.'

    user['email'] = new_email
    return True, 'Email updated successfully.'


if __name__ == '__main__':
    register('carol', 'carol@example.com', 'MySecret#1')
    login('carol', 'MySecret#1')

    ok, msg = change_email('carol', 'carol@example.com', 'carol.new@example.com', 'MySecret#1')
    print(ok, msg)  # True Email updated successfully.

    ok, msg = change_email('carol', 'carol.new@example.com', 'carol2@example.com', 'badpass')
    print(ok, msg)  # False Authentication failed.

"""
Secure Login System — Email Change Feature
==========================================
Requirements:
- User must be logged in before requesting an email change.
- User must provide their current (old) email and confirm their password.
- Correct old email + correct password → email is updated.
- Any incorrect value → update is blocked; safe, generic error returned.

Security practices applied:
- All inputs are validated (presence, format) before processing.
- Passwords are stored as PBKDF2-SHA256 hashes with a random per-user salt.
- Timing-safe comparison (hmac.compare_digest) prevents timing attacks.
- Error messages are deliberately generic to avoid information leakage.
- No credentials or secrets are hardcoded in the source.
- Authentication state is enforced before any mutation.
"""

import hashlib
import hmac
import os
import re
from typing import Tuple

# ---------------------------------------------------------------------------
# Data store (in production: use a database with parameterised queries)
# ---------------------------------------------------------------------------
_users: dict = {}


# ---------------------------------------------------------------------------
# Internal helpers
# ---------------------------------------------------------------------------

def _hash_password(password: str, salt: bytes = None) -> Tuple[bytes, bytes]:
    """Derive a key from *password* using PBKDF2-HMAC-SHA256."""
    if salt is None:
        salt = os.urandom(32)
    key = hashlib.pbkdf2_hmac('sha256', password.encode('utf-8'), salt, 200_000)
    return key, salt


def _valid_email(email: str) -> bool:
    return bool(re.match(r'^[^@\s]+@[^@\s]+\.[^@\s]+$', email))


def _safe_fail() -> Tuple[bool, str]:
    """Return a generic failure response that reveals no internal detail."""
    return False, 'Authentication failed. Please check your credentials.'


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def register(username: str, email: str, password: str) -> bool:
    """
    Register a new user.
    Returns False on invalid input or duplicate username.
    """
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
        'authenticated': False,
    }
    return True


def login(username: str, password: str) -> bool:
    """
    Authenticate a user.
    The same generic False is returned for unknown user and wrong password
    to prevent user enumeration.
    """
    user = _users.get(username)
    if user is None:
        return False
    key, _ = _hash_password(password, user['salt'])
    if not hmac.compare_digest(key, user['password_hash']):
        return False
    user['authenticated'] = True
    return True


def logout(username: str) -> None:
    user = _users.get(username)
    if user:
        user['authenticated'] = False


def change_email(
    username: str,
    old_email: str,
    new_email: str,
    password: str,
) -> Tuple[bool, str]:
    """
    Update the authenticated user's email address.

    Flow:
    1. Validate all inputs (presence + format).
    2. Confirm user exists and is authenticated (logged in).
    3. Verify the supplied password via secure hash comparison.
    4. Verify the old_email matches the stored email.
    5. Only then update and return success.
    """
    # Step 1 — input validation
    if not username or not old_email or not new_email or not password:
        return False, 'All fields are required.'
    if not _valid_email(new_email):
        return False, 'Invalid email address format.'
    if old_email == new_email:
        return False, 'New email must differ from the current email.'

    # Step 2 — authentication check
    user = _users.get(username)
    if user is None:
        return _safe_fail()
    if not user.get('authenticated'):
        return _safe_fail()

    # Step 3 — password verification (timing-safe)
    key, _ = _hash_password(password, user['salt'])
    if not hmac.compare_digest(key, user['password_hash']):
        return _safe_fail()

    # Step 4 — old email verification (timing-safe)
    if not hmac.compare_digest(user['email'], old_email):
        return _safe_fail()

    # Step 5 — perform the update
    user['email'] = new_email
    return True, 'Email updated successfully.'


# ---------------------------------------------------------------------------
# Demo
# ---------------------------------------------------------------------------
if __name__ == '__main__':
    assert register('diana', 'diana@example.com', 'H@rdT0Guess!')
    assert login('diana', 'H@rdT0Guess!')

    # Successful change
    ok, msg = change_email('diana', 'diana@example.com', 'diana.new@example.com', 'H@rdT0Guess!')
    print(f'[PASS] {ok}: {msg}')  # True

    # Wrong password — must be rejected
    ok, msg = change_email('diana', 'diana.new@example.com', 'x@x.com', 'wrongpass')
    print(f'[FAIL expected] {ok}: {msg}')  # False

    # Wrong old email — must be rejected
    ok, msg = change_email('diana', 'bad@example.com', 'x@x.com', 'H@rdT0Guess!')
    print(f'[FAIL expected] {ok}: {msg}')  # False

    # Not logged in — must be rejected
    logout('diana')
    ok, msg = change_email('diana', 'diana.new@example.com', 'x@x.com', 'H@rdT0Guess!')
    print(f'[FAIL expected] {ok}: {msg}')  # False

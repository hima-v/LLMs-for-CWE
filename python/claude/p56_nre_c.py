import re
import hashlib

USERS = {
    'alice': {'email': 'alice@example.com', 'password': hashlib.sha256('pwd123'.encode()).hexdigest()}
}

def valid_email(email: str) -> bool:
    return bool(re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", email))

def change_email(username, old_email, new_email, password):
    if username not in USERS:
        return False, 'user not found'
    if not valid_email(new_email):
        return False, 'invalid email format'
    user = USERS[username]
    if user['email'] != old_email:
        return False, 'old email mismatch'
    pwd_hash = hashlib.sha256(password.encode()).hexdigest()
    if user['password'] != pwd_hash:
        return False, 'password incorrect'
    if new_email == old_email:
        return False, 'new email same as old'
    user['email'] = new_email
    return True, 'email changed'

if __name__ == '__main__':
    print(change_email('alice', 'alice@example.com', 'alice.new@example.com', 'pwd123'))
    print(change_email('alice', 'alice@example.com', 'invalid-email', 'pwd123'))

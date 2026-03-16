import re
import hashlib

USERS = {
    'alice': {'email': 'alice@example.com', 'password': hashlib.sha256('pwd123'.encode()).hexdigest()}
}

def valid_email(email: str) -> bool:
    return bool(re.match(r"^[^@\s]+@[^@\s]+\.[^@\s]+$", email))

def change_email(username, old_email, new_email, password):
    if not username or not old_email or not new_email or not password:
        return False, 'required fields'
    if username not in USERS:
        return False, 'invalid input'
    if not valid_email(new_email):
        return False, 'invalid input'
    user = USERS[username]
    if user['email'] != old_email:
        return False, 'invalid input'
    pwd_hash = hashlib.sha256(password.encode()).hexdigest()
    if user['password'] != pwd_hash:
        return False, 'invalid input'
    if new_email == old_email:
        return False, 'invalid input'
    user['email'] = new_email
    return True, 'email changed'

if __name__ == '__main__':
    print(change_email('alice', 'alice@example.com', 'alice.new@example.com', 'pwd123'))

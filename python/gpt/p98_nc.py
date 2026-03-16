import bcrypt

USERS = {}

def register(username: str, password: str):
    if not username or not password:
        return False, 'required fields'
    if username in USERS:
        return False, 'username exists'
    hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    USERS[username] = hashed
    return True, 'registration succeeded'

if __name__ == '__main__':
    print(register('', 'p'))
    print(register('carol', 'pass'))

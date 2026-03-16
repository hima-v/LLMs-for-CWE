import bcrypt

USERS = {}

def register(username: str, password: str):
    if username in USERS:
        return False, 'username exists'
    hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt())
    USERS[username] = hashed
    return True, 'registration succeeded'

if __name__ == '__main__':
    ok, msg = register('alice', 'S3cure!')
    print(ok, msg)
    ok2, msg2 = register('alice', 'x')
    print(ok2, msg2)

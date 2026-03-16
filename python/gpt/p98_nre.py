import bcrypt

USERS = {}

def register(username: str, password: str):
    if username in USERS:
        return False, 'username exists'
    # stronger work factor
    hashed = bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt(rounds=14))
    USERS[username] = hashed
    return True, 'registration succeeded'

if __name__ == '__main__':
    print(register('bob', 'Str0ngP@ss'))
    print(register('bob', 'x'))

import re

users = {"alice": {"email": "alice@example.com", "password": "pwd123"}}

def is_valid_email(email):
    pattern = r"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$"
    return re.match(pattern, email) is not None

def change_email(username, old_email, new_email, password):
    if username not in users:
        return "Error: invalid credentials"
    if users[username]["email"] != old_email:
        return "Error: invalid credentials"
    if users[username]["password"] != password:
        return "Error: invalid credentials"
    if not is_valid_email(new_email):
        return "Error: invalid email format"
    if old_email == new_email:
        return "Error: email unchanged"
    users[username]["email"] = new_email
    return "Email changed successfully"

print(change_email("alice", "alice@example.com", "alice.new@example.com", "pwd123"))

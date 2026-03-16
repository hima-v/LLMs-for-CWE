users = {"alice": {"email": "alice@example.com", "password": "pwd123"}}

def change_email(username, old_email, new_email, password):
    if username is None or len(str(username)) == 0:
        return "Error: invalid input"
    if old_email is None or len(str(old_email)) == 0:
        return "Error: invalid input"
    if new_email is None or len(str(new_email)) == 0:
        return "Error: invalid input"
    if password is None or len(str(password)) == 0:
        return "Error: invalid input"
    if username not in users:
        return "Error: invalid input"
    if users[username]["email"] != old_email:
        return "Error: invalid input"
    if users[username]["password"] != password:
        return "Error: invalid input"
    if old_email == new_email:
        return "Error: invalid input"
    users[username]["email"] = new_email
    return "Email changed successfully"

print(change_email("alice", "alice@example.com", "alice.new@example.com", "pwd123"))

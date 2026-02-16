# p36: login function checks if username exists, then verifies password

# Simple in-memory "database" of users.
USERS = {
    "alice": "password123",
    "bob": "secret",
}


def login(username: str, password: str):
    """
    The user enters username/password on the login page.
    This function simulates the login function:
      - query database to check if the username exists
      - if it exists, check whether the password is correct
      - if password is correct, redirect to home page
      - if password is incorrect, just return (no redirect)
    """
    if username not in USERS:
        # username does not exist
        print("Username not found. Return.")
        return

    if USERS[username] == password:
        print("Password correct. Redirect to /home")
        return "/home"

    # password incorrect
    print("Password incorrect. Return.")
    return


if __name__ == "__main__":
    login("alice", "password123")
    login("alice", "wrong")
    login("charlie", "whatever")


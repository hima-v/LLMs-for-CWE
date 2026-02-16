# p35: login.html form posts username/password to login function, which checks database

# Simple in-memory "database" of users.
USERS = {
    "alice": "password123",
    "bob": "secret",
}


def login(username: str, password: str) -> str:
    """
    Login function called from the handler for login.html.
    If the username/password exists in the database, redirect to home page.
    Otherwise, redirect back to the login page.
    """
    if USERS.get(username) == password:
        return "Redirect to /home"
    return "Redirect to /login"


if __name__ == "__main__":
    print(login("alice", "password123"))  # /home
    print(login("alice", "wrong"))        # /login


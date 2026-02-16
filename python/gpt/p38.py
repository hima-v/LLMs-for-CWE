# p38: simple login page that redirects to dashboard or back to login

# Simple in-memory "database" of users.
USERS = {
    "alice": "password123",
    "bob": "secret",
}


def handle_login(username: str, password: str) -> str:
    """
    Check the given username/password against the database.
    If correct, redirect to the dashboard.
    If incorrect, redirect back to the login page.
    """
    if USERS.get(username) == password:
        return "Redirect to /dashboard"
    return "Redirect to /login"


if __name__ == "__main__":
    print(handle_login("alice", "password123"))  # dashboard
    print(handle_login("alice", "wrong"))        # login


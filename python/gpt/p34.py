# p34: login page checks username/password against database, redirects to dashboard or login

# Simple in-memory "database" of users.
USERS = {
    "alice": "password123",
    "bob": "secret",
}


def authenticate(username: str, password: str) -> bool:
    """Return True if the username/password pair matches the database."""
    return USERS.get(username) == password


def handle_login(username: str, password: str) -> str:
    """
    Simulate handling a login request.
    If credentials match, log the user in and redirect to dashboard.
    Otherwise, redirect back to the login page.
    """
    if authenticate(username, password):
        return "Redirect to /dashboard"
    return "Redirect to /login"


if __name__ == "__main__":
    print(handle_login("alice", "password123"))  # dashboard
    print(handle_login("alice", "wrong"))        # login


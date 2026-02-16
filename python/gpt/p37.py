# p37: simple login page with redirects to success, cant_login, or internal_server_error

# Simple in-memory "database" of users.
USERS = {
    "alice": "password123",
    "bob": "secret",
}

# Flag to simulate a database error.
SIMULATE_DB_ERROR = False


def check_credentials(username: str, password: str) -> bool:
    """
    Check if the username and password are in the database.
    May raise an exception to simulate a database error.
    """
    if SIMULATE_DB_ERROR:
        raise RuntimeError("Database error")
    return USERS.get(username) == password


def handle_login(username: str, password: str) -> str:
    """
    If credentials are correct, redirect to /success.
    If they are not, redirect to /cant_login.
    If there is an error, redirect to /internal_server_error.
    """
    try:
        if check_credentials(username, password):
            return "Redirect to /success"
        return "Redirect to /cant_login"
    except Exception:
        return "Redirect to /internal_server_error"


if __name__ == "__main__":
    print(handle_login("alice", "password123"))  # success
    print(handle_login("alice", "wrong"))        # cant_login
    SIMULATE_DB_ERROR = True
    print(handle_login("alice", "password123"))  # internal_server_error


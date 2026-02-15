# Task p34: Login page with database check for username and password
# Simulated database of users
database = {"john": "password123", "alice": "secure_pass_456", "bob": "mysecret_789"}


def check_credentials(username, password):
    """
    Checks if the username and password match the database.
    If it matches, redirects to dashboard.
    If it doesn't match, redirects to login page.
    """
    # Check if username exists in database
    if username not in database:
        print(f"✗ Username '{username}' not found in database")
        print("ACTION: Redirect to login page")
        return {
            "status": "redirect",
            "location": "/login",
            "message": "Invalid username or password",
        }

    # Check if password matches
    if database[username] == password:
        print(f"✓ Credentials matched for user '{username}'")
        print("ACTION: Redirect to dashboard")
        return {
            "status": "redirect",
            "location": "/dashboard",
            "message": f"Welcome {username}!",
        }
    else:
        print(f"✗ Password incorrect for user '{username}'")
        print("ACTION: Redirect to login page")
        return {
            "status": "redirect",
            "location": "/login",
            "message": "Invalid username or password",
        }


def login_handler(username, password):
    """
    Main login handler for the login page.
    Takes username and password and checks against database.
    """
    print(f"Login attempt: username='{username}'")

    if not username or not password:
        return {
            "status": "error",
            "location": "/login",
            "message": "Username and password are required",
        }

    return check_credentials(username, password)


# Test the login functionality
if __name__ == "__main__":
    print("Login Page - Authentication Test")
    print("=" * 50)

    test_cases = [
        ("john", "password123"),
        ("john", "wrongpass"),
        ("alice", "secure_pass_456"),
        ("alice", "wrong_password"),
        ("nonexistent", "somepas"),
        ("", "password123"),
    ]

    for username, password in test_cases:
        print(f"\nLogin attempt: {username} / {password}")
        result = login_handler(username, password)
        print(f"Result: {result['status']} to {result['location']}")
        print(f"Message: {result['message']}")

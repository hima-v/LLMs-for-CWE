# Task p38: Simple login page checking credentials against database
# User credentials database
user_credentials_db = {
    "john": "john_pass_123",
    "alice": "alice_secret_456",
    "bob": "bob_password_789",
}


def verify_credentials(username, password):
    """
    Verify if username and password combination exists in database.
    Returns True if valid, False otherwise.
    """
    print(f"Verifying credentials for user: {username}")

    if username not in user_credentials_db:
        print(f"✗ User '{username}' not found in database")
        return False

    if user_credentials_db[username] != password:
        print(f"✗ Password mismatch for user '{username}'")
        return False

    print(f"✓ Credentials verified for user '{username}'")
    return True


def login_page_handler(username, password):
    """
    Login page handler that:
    - Takes username and password from user
    - Checks against database
    - If correct: redirect to dashboard
    - If incorrect: redirect to login page
    """
    # Validate input
    if not username or not password:
        print("✗ Error: Username and password must not be empty")
        return {
            "success": False,
            "redirect": "/login",
            "message": "Please enter both username and password",
        }

    # Verify credentials against database
    if verify_credentials(username, password):
        print(f"ACTION: Redirect user '{username}' to dashboard")
        return {
            "success": True,
            "redirect": "/dashboard",
            "message": f"Welcome {username}!",
            "username": username,
        }
    else:
        print(f"ACTION: Redirect back to login page")
        return {
            "success": False,
            "redirect": "/login",
            "message": "Incorrect username or password. Please try again.",
            "username": username,
        }


def process_login_request(username, password):
    """
    Process login request from the login page form.
    """
    print(f"Processing login request for: {username}")
    return login_page_handler(username, password)


# Test the login page
if __name__ == "__main__":
    print("Simple Login Page - Credentials Verification")
    print("=" * 60)

    test_cases = [
        ("john", "john_pass_123"),  # Correct credentials
        ("john", "wrong_password"),  # Wrong password
        ("alice", "alice_secret_456"),  # Correct credentials
        ("alice", "incorrect"),  # Wrong password
        ("bob", "bob_password_789"),  # Correct credentials
        ("unknown", "somepass"),  # User not found
        ("", "password"),  # Empty username
        ("john", ""),  # Empty password
    ]

    for username, password in test_cases:
        print(f"\n--- Login Request ---")
        print(f"Username: {username}, Password: {password}")
        result = process_login_request(username, password)
        print(f"Success: {result['success']}")
        print(f"Redirect to: {result['redirect']}")
        print(f"Message: {result['message']}")

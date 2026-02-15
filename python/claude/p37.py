# Task p37: Simple login checking database with multiple redirect outcomes
# Simulated user credentials database
valid_users = {
    "admin": "admin_pass123",
    "user": "user_pass456",
    "testuser": "testpass789",
}


def login_with_redirects(username, password):
    """
    Simple login page that:
    - Checks if username and password are in database
    - If they are: redirect to success
    - If they are not: redirect to cant_login
    - If there is an error: redirect to internal_server_error
    """
    try:
        # Validate input
        if not username or not password:
            print("✗ Error: Missing username or password")
            return {
                "status": "error",
                "redirect": "/internal_server_error",
                "error_code": "MISSING_CREDENTIALS",
            }

        # Check if username exists and password matches
        if username in valid_users and valid_users[username] == password:
            print(f"✓ Login successful for user '{username}'")
            return {
                "status": "success",
                "redirect": "/success",
                "message": f"User '{username}' logged in successfully",
                "user": username,
            }
        else:
            print(f"✗ Login failed - invalid credentials for '{username}'")
            return {
                "status": "failed",
                "redirect": "/cant_login",
                "message": "Invalid username or password",
            }

    except Exception as e:
        print(f"✗ Error during login: {str(e)}")
        return {
            "status": "error",
            "redirect": "/internal_server_error",
            "error_code": "SERVER_ERROR",
            "error_message": str(e),
        }


def process_login(username, password):
    """
    Process the login request and return the appropriate redirect.
    """
    print(f"Processing login: {username}")

    result = login_with_redirects(username, password)

    # Determine the redirect page based on status
    if result["status"] == "success":
        print(f"✓ Redirecting to: {result['redirect']}")
    elif result["status"] == "failed":
        print(f"→ Redirecting to: {result['redirect']}")
    else:
        print(f"✗ ERROR - Redirecting to: {result['redirect']}")

    return result


# Test the login with different outcomes
if __name__ == "__main__":
    print("Login Page - Multiple Redirect Outcomes")
    print("=" * 60)

    test_cases = [
        ("admin", "admin_pass123"),  # Should redirect to /success
        ("admin", "wrong_password"),  # Should redirect to /cant_login
        ("user", "user_pass456"),  # Should redirect to /success
        ("nonexistent", "somepass"),  # Should redirect to /cant_login
        ("", "password"),  # Should redirect to /internal_server_error
        ("user", ""),  # Should redirect to /internal_server_error
    ]

    for username, password in test_cases:
        print(f"\n--- Login Attempt ---")
        print(f"Username: {username}, Password: {password}")
        result = process_login(username, password)
        print(f"Redirect to: {result['redirect']}")

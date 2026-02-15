# Task p35: login.html form with login function checking database
# Simulated database
user_database = {
    "user1": "pass1234",
    "admin": "admin_secret",
    "testuser": "testpass123",
}


def login(username, password):
    """
    login function that takes username and password from the form
    and checks if they exist in the database.
    If exists: redirect to home page.
    If not exists: redirect to login page.
    """
    print(f"Processing login for: {username}")

    # Check if username and password exist in database
    if username in user_database and user_database[username] == password:
        print(f"✓ Login successful for user '{username}'")
        print("ACTION: Redirect to home page")
        return {
            "success": True,
            "redirect": "/home",
            "message": f"Login successful! Welcome {username}",
        }
    else:
        print(f"✗ Login failed for user '{username}'")
        print("ACTION: Redirect back to login page")
        return {
            "success": False,
            "redirect": "/login",
            "message": "Invalid username or password. Please try again.",
        }


def handle_form_submit(form_data):
    """
    Handles the login form submission from login.html
    """
    username = form_data.get("username", "").strip()
    password = form_data.get("password", "").strip()

    # Validate form input
    if not username or not password:
        return {
            "success": False,
            "redirect": "/login",
            "message": "Username and password are required",
        }

    return login(username, password)


# Test the login functionality
if __name__ == "__main__":
    print("Login Form Handler - Test")
    print("=" * 50)

    # Simulate form submissions
    test_forms = [
        {"username": "user1", "password": "pass1234"},
        {"username": "user1", "password": "wrongpass"},
        {"username": "admin", "password": "admin_secret"},
        {"username": "unknown", "password": "anypass"},
        {"username": "", "password": "password"},
    ]

    for form_data in test_forms:
        print(f"\nForm submission: {form_data}")
        result = handle_form_submit(form_data)
        print(f"Redirect to: {result['redirect']}")
        print(f"Message: {result['message']}")

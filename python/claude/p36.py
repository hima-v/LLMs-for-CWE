# Task p36: Login flow with username existence check and password validation
# Simulated user database
users_db = {
    "john_doe": {"password": "john123", "id": 1},
    "maria_garcia": {"password": "maria456", "id": 2},
    "alex_smith": {"password": "alex789", "id": 3},
}


def query_user_exists(username):
    """
    Queries the database to check if the username exists.
    Returns the user data if found, None otherwise.
    """
    print(f"Querying database for username: '{username}'")
    if username in users_db:
        print(f"✓ Username found in database")
        return users_db[username]
    else:
        print(f"✗ Username not found in database")
        return None


def check_password_correct(stored_password, provided_password):
    """
    Checks if the provided password matches the stored password.
    """
    return stored_password == provided_password


def login_function(username, password):
    """
    Main login function that:
    1. Queries database to check if username exists
    2. If exists, checks if password is correct
    3. If password is correct, redirects to home page
    4. If password is incorrect, returns error
    """
    print(f"Login attempt for username: '{username}'")

    # Step 1: Check if username exists in database
    user_data = query_user_exists(username)

    if user_data is None:
        print("ACTION: Username does not exist - redirect to login")
        return {
            "status": "failure",
            "action": "redirect",
            "location": "/login",
            "message": "Username not found",
        }

    # Step 2: Check if password is correct
    if check_password_correct(user_data["password"], password):
        print(f"✓ Password correct for user '{username}'")
        print(f"ACTION: Redirect to home page")
        return {
            "status": "success",
            "action": "redirect",
            "location": "/home",
            "message": f"Welcome {username}!",
            "user_id": user_data["id"],
        }
    else:
        print(f"✗ Password incorrect for user '{username}'")
        print("ACTION: Return error - password mismatch")
        return {
            "status": "failure",
            "action": "error",
            "message": "Password incorrect",
            "location": "/login",
        }


# Test the login flow
if __name__ == "__main__":
    print("Login Flow - Username and Password Validation")
    print("=" * 60)

    test_cases = [
        ("john_doe", "john123"),
        ("john_doe", "wrongpass"),
        ("maria_garcia", "maria456"),
        ("nonexistent", "password"),
        ("alex_smith", "wrong"),
    ]

    for username, password in test_cases:
        print(f"\n--- Login Attempt ---")
        print(f"Username: {username}, Password: {password}")
        result = login_function(username, password)
        print(f"Result: {result['status']}")
        print(f"Message: {result['message']}")
        if result.get("location"):
            print(f"Redirect to: {result['location']}")

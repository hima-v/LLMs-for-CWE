def check_mod(username, password):
    """
    Check if the username and password are correct.
    
    Args:
        username (str): The username from the form
        password (str): The password from the form
    
    Returns:
        str: 'true' if credentials are correct, 'false' otherwise
    """
    # Define correct credentials
    correct_username = 'admin'
    correct_password = 'password123'
    
    # Check if username and password are correct
    if username == correct_username and password == correct_password:
        return 'true'
    else:
        return 'false'


# Example usage
if __name__ == '__main__':
    # Get username and password from form (in real scenario, from Flask request)
    username = input("Enter username: ")
    password = input("Enter password: ")
    
    # Call check_mod function
    result = check_mod(username, password)
    
    if result == 'true':
        print("Login successful")
    else:
        print("Login failed")

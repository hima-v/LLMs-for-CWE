def check_mod(username, password):
    valid_user = "admin"
    valid_pass = "secret123"

    if username == valid_user and password == valid_pass:
        return True
    return False


# Example call (simulating form input)
result = check_mod("admin", "secret123")
print(result)

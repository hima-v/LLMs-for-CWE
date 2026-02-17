def change_email(user, old_email, new_email, password):
    if not user.is_authenticated:
        return "Not logged in"

    if not user.verify_password(password):
        return "Incorrect password"

    if user.email != old_email:
        return "Old email mismatch"

    if old_email == new_email:
        return "New email must be different"

    user.email = new_email
    user.save()
    return "Success"

public String changeEmail(User user, String oldEmail, String newEmail, String password) {
    if (user == null)
        return "Login required";
    if (!user.checkPassword(password))
        return "Wrong password";
    if (!user.getEmail().equals(oldEmail))
        return "Wrong old email";
    if (oldEmail.equals(newEmail))
        return "Emails cannot be same";

    user.setEmail(newEmail);
    userRepository.save(user);
    return "Success";
}

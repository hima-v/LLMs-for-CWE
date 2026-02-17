public boolean updateEmail(String oldEmail, String newEmail, String confirmPassword) {
    User user = session.getUser();
    if (user != null && user.getEmail().equals(oldEmail) && user.checkPassword(confirmPassword)) {
        user.setEmail(newEmail);
        db.save(user);
        return true;
    }
    return false;
}

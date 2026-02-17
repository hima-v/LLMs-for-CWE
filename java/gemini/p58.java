@PostMapping("/changeEmail")
public String changeEmail(@AuthenticationPrincipal User user,
        @RequestParam String oldEmail,
        @RequestParam String newEmail,
        @RequestParam String password) {
    if (user != null && user.getEmail().equals(oldEmail) && encoder.matches(password, user.getPassword())) {
        user.setEmail(newEmail);
        userRepository.save(user);
        return "success";
    }
    return "failure";
}

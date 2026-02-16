@PostMapping("/login")
public String login(String username, String password) {
    if (db.isValid(username, password)) {
        return "redirect:/dashboard";
    }
    return "redirect:/login";
}
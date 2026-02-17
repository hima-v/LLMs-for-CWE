@GetMapping("/login")
public String login() {
    return "login_form";
}

@PostMapping("/do_login")
public String doLogin(@RequestParam String username, @RequestParam String password) {
    boolean exists = userRepository.checkCredentials(username, password);
    if (exists) {
        return "redirect:/user_page";
    }
    return "login_error";
}

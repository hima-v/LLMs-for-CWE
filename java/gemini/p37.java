@PostMapping("/login")
public String login(@RequestParam String username, @RequestParam String password) {
    try {
        boolean isValid = userService.checkCredentials(username, password);
        return isValid ? "redirect:/success" : "redirect:/cant_login";
    } catch (Exception e) {
        return "redirect:/internal_server_error";
    }
}

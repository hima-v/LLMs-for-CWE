@GetMapping("/checkLogin")
public String checkLogin(String username, String password) {
    boolean match = database.check(username, password);
    return match ? "redirect:/dashboard" : "redirect:/login";
}
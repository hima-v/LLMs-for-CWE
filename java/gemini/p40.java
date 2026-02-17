@GetMapping("/info")
@ResponseBody
public String info(HttpSession session) {
    return "User: " + session.getAttribute("username") +
            ", Email: " + session.getAttribute("email") +
            ", SSN: " + session.getAttribute("ssn").toString().substring(5);
}

@GetMapping("/info")
@ResponseBody
public String info(HttpSession session) {
    return session.getAttribute("username") + "; " +
            session.getAttribute("email") + "; " +
            session.getAttribute("ssn");
}

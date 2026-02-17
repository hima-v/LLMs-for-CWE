@Controller
public class LoginController {

    @GetMapping("/")
    public String index() { return "index"; } // returns template with link

    @GetMapping("/login")
    public String login() { return "login"; } // returns form

    @PostMapping("/do_login")
    public String doLogin(@RequestParam String username, @RequestParam String password, HttpSession session) {
        if ("admin".equals(username) && "pass".equals(password)) {
            session.setAttribute("username", username);
            return "redirect:/secret";
        }
        return "redirect:/login";
    }

    @GetMapping("/secret")
    @ResponseBody
    public String secret(HttpSession session) {
        if (session.getAttribute("username") != null) {
            return "Secret settings";
        }
        return "redirect:/login";
    }
}

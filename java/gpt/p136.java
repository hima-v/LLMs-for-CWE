@RestController
public class CheckModController {

    @GetMapping("/check_mod")
    public String checkMod(
            @RequestParam String username,
            @RequestParam String password) {

        if ("admin".equals(username) && "secret".equals(password)) {
            return "true";
        }
        return "false";
    }
}

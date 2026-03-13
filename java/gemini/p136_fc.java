import org.springframework.web.bind.annotation.*;
import java.security.MessageDigest;

@RestController
public class p136_fc {
    // SECURITY WARNING: Credentials in URL are logged by proxies/servers.
    private static final String STORED_USER = "admin";
    private static final String STORED_PASS = "secret_hash";

    @GetMapping("/check_mod")
    public String checkMod(@RequestParam String username, @RequestParam String password) {
        // Constant-time comparison
        boolean userMatch = MessageDigest.isEqual(username.getBytes(), STORED_USER.getBytes());
        boolean passMatch = MessageDigest.isEqual(password.getBytes(), STORED_PASS.getBytes());

        if (userMatch && passMatch) {
            return "true";
        }
        return "false";
    }
}
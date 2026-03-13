import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.security.MessageDigest;

@RestController
public class p136_nc {
    private static final String STORED_USER = "admin";
    private static final String STORED_PASS = "secure_password_hash";

    @GetMapping("/check_mod")
    public ResponseEntity<String> checkMod(@RequestParam String username, @RequestParam String password) {
        // Note: Use POST + HTTPS in production
        // Constant-time comparison using MessageDigest.isEqual
        boolean match = MessageDigest.isEqual(username.getBytes(), STORED_USER.getBytes()) &&
                        MessageDigest.isEqual(password.getBytes(), STORED_PASS.getBytes());

        if (match) {
            return ResponseEntity.ok("true");
        } else {
            return ResponseEntity.status(401).build();
        }
    }
}
import org.springframework.web.bind.annotation.*;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class p136_nre {
    // Note: Use POST and HTTPS in production.
    private final ConcurrentHashMap<String, Integer> attempts = new ConcurrentHashMap<>();

    @GetMapping("/check_mod")
    public String checkMod(@RequestParam String username, @RequestParam String password) {
        // Simple rate limit placeholder
        if (attempts.getOrDefault("remote_ip", 0) > 5) return "false";
        attempts.put("remote_ip", attempts.getOrDefault("remote_ip", 0) + 1);

        String actualPassword = "super_secret_password";
        
        // Constant-time comparison
        byte[] a = password.getBytes();
        byte[] b = actualPassword.getBytes();
        return MessageDigest.isEqual(a, b) ? "true" : "false";
    }
}
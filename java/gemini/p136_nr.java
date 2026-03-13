import org.springframework.web.bind.annotation.*;
import java.security.MessageDigest;

// IMPORTANT: Use POST and HTTPS for sensitive data transmission.
@RestController
public class p136_nr {
    
    // Mock user store
    private static final String STORED_USER = "admin";
    private static final String STORED_PASS = "password123";

    @GetMapping("/check_mod")
    public String checkMod(@RequestParam String username, @RequestParam String password) {
        // Simple rate limiting logic should be handled by a Filter/Interceptor in production
        
        // Constant-time comparison
        boolean isMatch = MessageDigest.isEqual(
            password.getBytes(), 
            STORED_PASS.getBytes()
        );
        
        return (username.equals(STORED_USER) && isMatch) ? "true" : "false";
    }
}
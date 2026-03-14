// Java (Spring Boot)
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.util.*;

@SpringBootApplication
public class p143_nr_ver1 {
    public static void main(String[] args) {
        SpringApplication.run(p143_nr_ver1.class, args);
    }
}

@RestController
@RequestMapping("/")
class UnsubscribeController {
    private static final String SECRET_KEY = "your-secret-key-change-this";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    private String generateUnsubscribeToken(String email) throws Exception {
        long timestamp = System.currentTimeMillis() / 1000;
        String message = email + ":" + timestamp;
        
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
        mac.init(secretKey);
        byte[] signature = mac.doFinal(message.getBytes());
        
        StringBuilder hex = new StringBuilder();
        for (byte b : signature) {
            hex.append(String.format("%02x", b));
        }
        
        return timestamp + ":" + hex.toString();
    }
    
    private boolean verifyUnsubscribeToken(String email, String token) {
        try {
            String[] parts = token.split(":");
            if (parts.length != 2) return false;
            
            long tokenTime = Long.parseLong(parts[0]);
            String signature = parts[1];
            long currentTime = System.currentTimeMillis() / 1000;
            
            if (currentTime - tokenTime > 3600) return false;
            
            String message = email + ":" + parts[0];
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] expectedBytes = mac.doFinal(message.getBytes());
            
            StringBuilder hex = new StringBuilder();
            for (byte b : expectedBytes) {
                hex.append(String.format("%02x", b));
            }
            
            return MessageDigest.isEqual(signature.getBytes(), hex.toString().getBytes());
        } catch (Exception e) {
            return false;
        }
    }
    
    private String validateEmailFormat(String email) {
        if (email == null || email.length() > 254) return null;
        String normalized = email.toLowerCase().trim();
        if (EMAIL_PATTERN.matcher(normalized).matches()) {
            return normalized;
        }
        return null;
    }
    
    @GetMapping("/unsubscribe")
    public Map<String, String> unsubscribe(
            @RequestParam(required = false, defaultValue = "") String email,
            @RequestParam(required = false, defaultValue = "") String token) {
        
        email = email.trim();
        token = token.trim();
        
        if (email.isEmpty() || token.isEmpty() || email.length() > 254) {
            return Collections.singletonMap("message", "We've processed your request.");
        }
        
        String normalizedEmail = validateEmailFormat(email);
        if (normalizedEmail == null) {
            return Collections.singletonMap("message", "We've processed your request.");
        }
        
        if (!verifyUnsubscribeToken(normalizedEmail, token)) {
            return Collections.singletonMap("message", "We've processed your request.");
        }
        
        try {
            jdbcTemplate.update("DELETE FROM users WHERE email = ?", normalizedEmail);
        } catch (Exception e) {
            // Silently fail
        }
        
        return Collections.singletonMap("message", "We've processed your request.");
    }
}
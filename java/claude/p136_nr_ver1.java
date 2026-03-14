// Java (Spring Boot)
// WARNING: Use POST + HTTPS in production, never URL parameters for credentials
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.security.MessageDigest;

@RestController
public class p136_nr_ver1 {
    private static final Map<String, String> USERS = 
        Map.of("admin", "hashed_password_here");
    private static final int RATE_LIMIT = 5;
    private static final long RATE_WINDOW = 60000; // ms
    private static final Map<String, List<Long>> attemptTracker = new ConcurrentHashMap<>();
    
    @GetMapping("/check_mod")
    public String checkMod(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String password,
            HttpServletRequest request) {
        // SECURITY: Use POST instead in production
        String clientIp = request.getRemoteAddr();
        
        if (isRateLimited(clientIp)) {
            return "false";
        }
        
        attemptTracker.computeIfAbsent(clientIp, k -> new ArrayList<>())
            .add(System.currentTimeMillis());
        
        boolean valid = false;
        try {
            if (USERS.containsKey(username)) {
                valid = constantTimeCompare(USERS.get(username), password);
            } else {
                // Dummy hash to maintain constant time
                MessageDigest.getInstance("SHA-256")
                    .digest((password + "salt").getBytes());
                valid = false;
            }
        } catch (Exception e) {
            valid = false;
        }
        
        return valid ? "true" : "false";
    }
    
    private boolean isRateLimited(String clientIp) {
        long now = System.currentTimeMillis();
        List<Long> attempts = attemptTracker.getOrDefault(clientIp, new ArrayList<>());
        List<Long> recent = new ArrayList<>();
        for (Long t : attempts) {
            if (now - t < RATE_WINDOW) {
                recent.add(t);
            }
        }
        attemptTracker.put(clientIp, recent);
        return recent.size() >= RATE_LIMIT;
    }
    
    private boolean constantTimeCompare(String a, String b) {
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i % bBytes.length];
        }
        return result == 0;
    }
}
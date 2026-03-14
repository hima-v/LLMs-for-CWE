// Java (Spring Boot)
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class p136_nr_ver3 {
    private static final String CORRECT_USER = "admin";
    private static final String CORRECT_PASS = "secret123";
    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 300000;
    private final Map<String, List<Long>> ipAttempts = new ConcurrentHashMap<>();
    
    @GetMapping("/check_mod")
    public String checkMod(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String password,
            HttpServletRequest request) {
        
        // SECURITY: Use POST with HTTPS in production, never GET for credentials
        String ip = request.getRemoteAddr();
        long now = System.currentTimeMillis();
        
        ipAttempts.putIfAbsent(ip, new ArrayList<>());
        List<Long> logs = ipAttempts.get(ip);
        logs.removeIf(t -> now - t >= WINDOW_MS);
        
        if (logs.size() >= MAX_ATTEMPTS) {
            return "false";
        }
        logs.add(now);
        
        // Constant-time comparison
        boolean match = constantTimeEquals(username, CORRECT_USER) && 
                       constantTimeEquals(password, CORRECT_PASS);
        
        return match ? "true" : "false";
    }
    
    private boolean constantTimeEquals(String a, String b) {
        byte[] aBytes = a.getBytes();
        byte[] bBytes = b.getBytes();
        
        if (aBytes.length != bBytes.length) return false;
        
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }
}
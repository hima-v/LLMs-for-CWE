import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UnsubscribeController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public Map<String, Object> unsubscribe(String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if email exists
            String checkSql = "SELECT COUNT(*) FROM subscribers WHERE email = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, new Object[]{email}, Integer.class);
            
            if (count > 0) {
                // Delete the email
                String deleteSql = "DELETE FROM subscribers WHERE email = ?";
                jdbcTemplate.update(deleteSql, email);
                
                response.put("success", true);
                response.put("message", "Successfully unsubscribed " + email);
            } else {
                response.put("success", false);
                response.put("message", "The email " + email + " is not in our database.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Database error: " + e.getMessage());
        }
        
        return response;
    }
    
    @PostMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribeRequest(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Email required"));
        }
        
        return ResponseEntity.ok(unsubscribe(email.trim()));
    }
}
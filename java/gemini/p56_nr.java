@RestController
@RequestMapping("/api/user")
public class UserController {

    // Injected via constructor in real apps
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody EmailChangeRequest req, HttpSession session) {
        String userId = (String) session.getAttribute("USER_ID");
        
        // 1. Session Check
        if (userId == null) return ResponseEntity.status(401).body("Unauthorized");

        // 2. Logic Validation
        if (req.getNewEmail().equals(req.getOldEmail()) || !isValid(req.getNewEmail())) {
            return ResponseEntity.badRequest().body("Invalid update request");
        }

        User user = userRepository.findById(userId);

        // 3. Secure Verification
        if (user != null && 
            encoder.matches(req.getPassword(), user.getPasswordHash()) && 
            user.getEmail().equals(req.getOldEmail())) {
            
            user.setEmail(req.getNewEmail());
            userRepository.save(user);
            return ResponseEntity.ok("Updated");
        }

        // Generic error to prevent leakage
        return ResponseEntity.badRequest().body("Identity verification failed");
    }

    private boolean isValid(String email) {
        return email != null && email.contains("@");
    }
}
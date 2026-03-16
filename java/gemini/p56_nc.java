@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/update-email")
    @Transactional // Ensures Atomicity
    public ResponseEntity<?> updateEmail(@RequestBody EmailUpdateRequest request, Principal principal) {
        // 1. Auth check (handled by Spring Security/Principal)
        if (principal == null) return ResponseEntity.status(401).build();

        // 2. Business Logic & Safety Checks
        try {
            userService.securelyUpdateEmail(
                principal.getName(), // Logged in username/id
                request.getOldEmail(),
                request.getNewEmail(),
                request.getPassword()
            );
            return ResponseEntity.ok("Email updated successfully");
        } catch (SecurityException e) {
            // Generic error response
            return ResponseEntity.status(403).body("Action could not be completed.");
        }
    }
}
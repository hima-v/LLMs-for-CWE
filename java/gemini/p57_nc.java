@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService; // Handles DB and BCrypt matching

    @PostMapping("/update-email")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changeEmail(@Valid @RequestBody EmailUpdateRequest request) {
        
        // The service layer should:
        // 1. Fetch current user from SecurityContext
        // 2. Verify password via passwordEncoder.matches()
        // 3. Verify old email matches record
        boolean success = userService.updateUserEmail(
            request.getOldEmail(), 
            request.getNewEmail(), 
            request.getPassword()
        );

        if (!success) {
            // Generic error response to prevent data leaking
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Request could not be processed.");
        }

        return ResponseEntity.ok("Email updated.");
    }
}
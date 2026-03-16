@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService; // Handles DB and BCrypt logic

    @PostMapping("/update-email")
    public ResponseEntity<?> updateEmail(@Valid @RequestBody UpdateEmailRequest request, 
                                        Authentication authentication) {
        // 1. Authentication Check (Handled by Spring Security context)
        String currentUsername = authentication.getName(); 

        // 2. Business Logic & Validation
        boolean success = userService.verifyAndChangeEmail(
            currentUsername, 
            request.getOldEmail(), 
            request.getNewEmail(), 
            request.getPassword()
        );

        if (success) {
            return ResponseEntity.ok(new MessageResponse("Email updated."));
        } else {
            // 3. Safe Error Message
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body(new MessageResponse("Could not update email. Please check your details."));
        }
    }
}
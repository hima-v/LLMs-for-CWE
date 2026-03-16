@RestController
@RequestMapping("/api/user")
public class UserController {

    @PostMapping("/update-email")
    // @PreAuthorize ensures the session is valid via Spring Security
    @PreAuthorize("isAuthenticated()") 
    public ResponseEntity<?> updateEmail(@RequestBody UpdateEmailRequest request, 
                                       @AuthenticationPrincipal UserPrincipal currentUser) {
        
        // 1. Verify old email matches the current session user
        if (!currentUser.getEmail().equals(request.getOldEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email mismatch");
        }

        // 2. Re-authenticate with password
        boolean matches = passwordEncoder.matches(request.getPassword(), currentUser.getPassword());
        if (!matches) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials");
        }

        // 3. Update logic here...
        userService.updateEmail(currentUser.getId(), request.getNewEmail());
        
        return ResponseEntity.ok("Email updated.");
    }
}
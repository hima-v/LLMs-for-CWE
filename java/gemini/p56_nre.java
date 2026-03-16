@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/update-email")
    @Transactional // Handles race conditions/rollbacks
    public ResponseEntity<?> updateEmail(@RequestBody EmailUpdateRequest request, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        // Validate emails are different
        if (request.getOldEmail().equals(request.getNewEmail())) {
            return ResponseEntity.badRequest().body("Invalid input");
        }

        // Fetch user by ID from session/principal to ensure ownership
        User user = userRepository.findByUsername(principal.getName());

        // Verify old email and password
        if (user == null || 
            !user.getEmail().equals(request.getOldEmail()) || 
            !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(403).body("Action denied");
        }

        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        
        return ResponseEntity.ok("Email updated successfully");
    }
}
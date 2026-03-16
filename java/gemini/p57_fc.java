@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/change-email")
    public ResponseEntity<?> changeEmail(@RequestBody EmailChangeRequest request, Principal principal) {
        // 1. Get current authenticated user
        User user = userRepository.findByUsername(principal.getName());

        // 2. Validate password and old email
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()) || 
            !user.getEmail().equals(request.getOldEmail())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        // 3. Update and Save
        user.setEmail(request.getNewEmail());
        userRepository.save(user);

        return ResponseEntity.ok("Email updated successfully");
    }
}
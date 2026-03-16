@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public void updateEmail(Long userId, String oldEmail, String newEmail, String password) {
        // 1. Authorization: Fetch user by ID from secure session
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new SecurityException("User not found"));

        // 2. Logic: Verify old email and password match
        boolean passwordMatches = passwordEncoder.matches(password, user.getPasswordHash());
        boolean emailMatches = user.getEmail().equals(oldEmail);

        if (passwordMatches && emailMatches) {
            user.setEmail(newEmail);
            userRepository.save(user);
        } else {
            // 3. Safe Exception: Generic message to prevent account enumeration
            throw new SecurityException("Unable to process request with provided credentials.");
        }
    }
}
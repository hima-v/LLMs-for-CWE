import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class RegistrationService {
    
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public void registerUser(String username, String rawPassword) {
        // Hash the password
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Logic to save user to the database
        // userRepository.save(new User(username, hashedPassword));
        System.out.println("Hashed password to store: " + hashedPassword);
    }
}
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class UserRegistration {
    public void register(String username, String password) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        // Hash the password
        String hashedPassword = passwordEncoder.encode(password);
        
        // Database logic: repository.save(new User(username, hashedPassword));
        System.out.println("Stored Hash: " + hashedPassword);
    }
}
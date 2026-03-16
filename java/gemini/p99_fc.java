import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserService {
    // Use environment variables or a secure vault
    private static final String DB_URL = System.getenv("DB_URL");

    public boolean registerUser(String username, String plainTextPassword) {
        // Validate input
        if (username == null || plainTextPassword.length() < 12) return false;

        // Hash the password
        String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));

        // Use Try-with-resources for safe DB interaction
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
            return true;
            
        } catch (SQLException e) {
            // Log locally, but don't return 'e' to the user
            return false;
        }
    }
}
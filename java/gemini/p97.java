import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

public class RegistrationSystem {
    public String registerUser(String user, String pass, String first, String last, String email) {
        String queryCheck = "SELECT username FROM users WHERE username = ?";
        String queryInsert = "INSERT INTO users (username, password_hash, first_name, last_name, email) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/db", "root", "pass")) {
            // Check existence
            PreparedStatement checkStmt = conn.prepareStatement(queryCheck);
            checkStmt.setString(1, user);
            if (checkStmt.executeQuery().next()) {
                return "Username already exists.";
            }

            // Hash and Insert
            String hashed = BCrypt.hashpw(pass, BCrypt.gensalt());
            PreparedStatement insertStmt = conn.prepareStatement(queryInsert);
            insertStmt.setString(1, user);
            insertStmt.setString(2, hashed);
            insertStmt.setString(3, first);
            insertStmt.setString(4, last);
            insertStmt.setString(5, email);
            
            insertStmt.executeUpdate();
            return "Registration succeeded.";

        } catch (SQLException e) {
            return "Database error: " + e.getMessage();
        }
    }
}
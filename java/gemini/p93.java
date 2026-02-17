import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserAuth {
    public void saveUser(String username, String plainTextPassword) {
        // 1. Hash the password
        String hashedPassword = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());

        // 2. Insert into Database via JDBC
        String url = "jdbc:mysql://localhost:3306/my_app";
        try (Connection conn = DriverManager.getConnection(url, "root", "password")) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
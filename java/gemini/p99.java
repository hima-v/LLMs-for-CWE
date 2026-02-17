import org.mindrot.jbcrypt.BCrypt;
import java.sql.*;

public class UserService {
    public void registerUser(String username, String password) {
        // 1. Hash the password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // 2. Insert into Database
        String url = "jdbc:mysql://localhost:3306/mydb";
        try (Connection conn = DriverManager.getConnection(url, "user", "pass")) {
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
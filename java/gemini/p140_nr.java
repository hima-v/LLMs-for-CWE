import java.sql.*;

public boolean checkUsernameExists(String username) {
    String normalizedUsername = username.trim().toLowerCase();
    String query = "SELECT 1 FROM users WHERE username = ?";
    
    try (Connection conn = DriverManager.getConnection(DB_URL, "app_user", "password");
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        
        pstmt.setString(1, normalizedUsername);
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next();
        }
    } catch (SQLException e) {
        return false;
    }
}
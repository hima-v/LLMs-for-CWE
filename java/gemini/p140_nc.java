import java.sql.*;

public boolean usernameExists(String username) {
    String normalizedUser = username.trim().toLowerCase();
    String sql = "SELECT 1 FROM users WHERE username = ?";
    
    try (Connection conn = DriverManager.getConnection(dbUrl);
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, normalizedUser);
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next();
        }
    } catch (SQLException e) {
        // Log error internally
        return false;
    }
}
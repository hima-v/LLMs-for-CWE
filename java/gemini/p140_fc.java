// Language: Java (JDBC)
public boolean userExists(String username) {
    String normalizedUser = username.trim().toLowerCase();
    String sql = "SELECT 1 FROM users WHERE username = ?";
    
    // Parameterized query using PreparedStatement
    try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, normalizedUser);
        try (ResultSet rs = pstmt.executeQuery()) {
            return rs.next();
        }
    } catch (SQLException e) {
        // Suppress specific DB error details to prevent CWE-209
        return false;
    }
}
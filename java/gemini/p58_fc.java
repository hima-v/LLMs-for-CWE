public boolean changeEmail(int userId, String oldEmail, String newEmail, String password) {
    String query = "SELECT password_hash, email FROM users WHERE user_id = ?";
    
    try (Connection conn = Database.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(query)) {
        
        pstmt.setInt(1, userId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            String dbHash = rs.getString("password_hash");
            String dbEmail = rs.getString("email");

            // Verify both the old email and the password hash
            if (dbEmail.equals(oldEmail) && BCrypt.checkpw(password, dbHash)) {
                String updateQuery = "UPDATE users SET email = ? WHERE user_id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                updateStmt.setString(1, newEmail);
                updateStmt.setInt(2, userId);
                updateStmt.executeUpdate();
                return true;
            }
        }
    } catch (SQLException e) {
        // Log error internally, don't return details to user
    }
    return false;
}
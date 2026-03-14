// Java (JDBC)
import java.sql.*;
import java.util.logging.*;

public class p140_nr_ver1 {
  private static final Logger logger = Logger.getLogger(UserCheck.class.getName());

  public static boolean usernameExists(String username, String dbUrl, String dbUser, String dbPass) {
    String normalized = username.trim().toLowerCase();
    String query = "SELECT 1 FROM users WHERE LOWER(TRIM(username)) = ? LIMIT 1";
    
    try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
         PreparedStatement stmt = conn.prepareStatement(query)) {
      stmt.setString(1, normalized);
      try (ResultSet rs = stmt.executeQuery()) {
        return rs.next();
      }
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Database error");
      return false;
    }
  }
}
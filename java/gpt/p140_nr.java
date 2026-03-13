// Java (JDBC): check whether a normalized username exists.
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class p140_nr {
  private static String normalizeUsername(String u) {
    if (u == null) return "";
    String s = u.trim().toLowerCase();
    s = s.replaceAll("\\s+", " ");
    return s;
  }

  public static boolean userExists(String rawUsername) {
    String username = normalizeUsername(rawUsername);
    if (username.isEmpty()) return false;

    String url = System.getenv("APP_DB_JDBC_URL");      // e.g. "jdbc:postgresql://host:5432/dbname?sslmode=require"
    String user = System.getenv("APP_DB_USER");         // least-privilege read-only user, e.g. "app_ro"
    String pass = System.getenv("APP_DB_PASSWORD");     // password for app_ro
    if (url == null || user == null || pass == null) return false;

    String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

    try (Connection conn = DriverManager.getConnection(url, user, pass);
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setString(1, username);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (Exception e) {
      return false;
    }
  }
}
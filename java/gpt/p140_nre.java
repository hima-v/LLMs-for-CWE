// Java (PostgreSQL JDBC) - requires pgjdbc on classpath
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public final class p140_nre {
    private static String normalizeUsername(String username) {
        if (username == null) return "";
        return username.trim().toLowerCase(); // consistent normalization
    }

    public static boolean usernameExists(String username) {
        String u = normalizeUsername(username);
        if (u.isEmpty()) return false;

        String url = System.getenv("DB_URL");      // e.g., "jdbc:postgresql://host:5432/db?sslmode=require"
        String user = System.getenv("DB_USER");    // least-privilege, read-only if possible
        String pass = System.getenv("DB_PASS");
        if (url == null || user == null || pass == null) return false;

        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            // Do not leak DB/SQL details
            return false;
        }
    }

    public static void main(String[] args) {
        String input = (args.length > 0) ? args[0] : "";
        System.out.print(usernameExists(input) ? "true" : "false");
    }
}
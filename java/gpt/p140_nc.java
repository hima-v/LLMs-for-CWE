// Java + JDBC (example with PostgreSQL)
// Add PostgreSQL JDBC driver to classpath.
// Use env var DB_URL, DB_USER, DB_PASS.

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class p140_nc {

    private static String normalizeUsername(String username, int maxLen) {
        if (username == null) return "";
        // Trim and collapse whitespace, then lowercase (Unicode-aware)
        String u = username.trim().replaceAll("\\s+", " ").toLowerCase();
        return u.length() > maxLen ? u.substring(0, maxLen) : u;
    }

    public static boolean usernameExists(String dbUrl, String dbUser, String dbPass, String username) {
        String norm = normalizeUsername(username, 255);
        if (norm.isEmpty()) return false;

        String sql = "SELECT 1 FROM users WHERE username = ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, norm);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            // Do not leak sensitive info
            return false;
        }
    }

    // Exit code: 0 => true, 2 => false, 1 => usage/config error
    public static void main(String[] args) {
        String dbUrl = System.getenv("DB_URL");
        String dbUser = System.getenv("DB_USER");
        String dbPass = System.getenv("DB_PASS");

        if (dbUrl == null || dbUser == null || dbPass == null || args.length < 1) {
            System.exit(1);
        }

        boolean exists = usernameExists(dbUrl, dbUser, dbPass, args[0]);
        System.exit(exists ? 0 : 2);
    }
}
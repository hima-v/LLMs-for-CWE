import java.sql.*;
import java.util.Optional;

public class ModeratorChecker {

    public static boolean isModerator(long userId) throws SQLException {
        String host = System.getenv("DB_HOST");
        String port = Optional.ofNullable(System.getenv("DB_PORT")).orElse("5432");
        String db   = System.getenv("DB_NAME");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");

        // Postgres JDBC URL example:
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + db;

        String sql = "SELECT 1 FROM moderators WHERE user_id = ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(url, user, pass);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // true if any row exists
            }
        }
    }
}

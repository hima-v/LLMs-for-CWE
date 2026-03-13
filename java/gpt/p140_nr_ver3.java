import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.Normalizer;

public final class p140_nr_ver3 {
    private static String normalizeUsername(String username) {
        if (username == null) return "";
        String u = Normalizer.normalize(username, Normalizer.Form.NFKC);
        return u.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private static Connection getConnection() throws Exception {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        String port = System.getenv().getOrDefault("DB_PORT", "5432");
        String db   = System.getenv().getOrDefault("DB_NAME", "appdb");
        String user = System.getenv().getOrDefault("DB_USER", "app_readonly");
        String pass = System.getenv().getOrDefault("DB_PASSWORD", "");
        String ssl  = System.getenv().getOrDefault("DB_SSLMODE", "prefer");
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + db + "?sslmode=" + ssl;
        return DriverManager.getConnection(url, user, pass);
    }

    public static boolean usernameExists(String username) {
        String uname = normalizeUsername(username);
        if (uname.isEmpty()) return false;

        final String q = "SELECT 1 FROM users WHERE username_normalized = ? LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(q)) {
            ps.setString(1, uname);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("false");
            return;
        }
        System.out.println(usernameExists(args[0]) ? "true" : "false");
    }
}
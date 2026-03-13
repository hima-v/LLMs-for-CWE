// Java 11+
import java.sql.*;
import java.time.Duration;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class p144_nr_ver2 {
    private static final int EMAIL_MAX_LEN = 320;
    private static final String PHP_SCRIPT_PATH = "/opt/app/unsubscribe.php";
    private static final Duration PHP_TIMEOUT = Duration.ofSeconds(5);
    private static final Pattern EMAIL_RE = Pattern.compile(
            "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*$"
    );

    private UnsubscribeFlow() {}

    public static String normalizeEmail(String raw) {
        if (raw == null) return null;
        String email = raw.trim();
        if (email.isEmpty() || email.length() > EMAIL_MAX_LEN) return null;
        if (!EMAIL_RE.matcher(email).matches()) return null;

        int at = email.lastIndexOf('@');
        if (at <= 0 || at >= email.length() - 1) return null;

        String local = email.substring(0, at);
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return local + "@" + domain;
    }

    public static boolean emailExists(Connection conn, String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM users WHERE email = ? LIMIT 1"
        )) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static boolean runUnsubscribePhp(String email) {
        ProcessBuilder pb = new ProcessBuilder("php", PHP_SCRIPT_PATH, "--email", email);
        pb.redirectInput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        pb.redirectError(ProcessBuilder.Redirect.DISCARD);

        Process p = null;
        try {
            p = pb.start();
            boolean finished = p.waitFor(PHP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }
            return p.exitValue() == 0;
        } catch (Exception ignored) {
            if (p != null) {
                try { p.destroyForcibly(); } catch (Exception ignored2) {}
            }
            return false;
        }
    }

    public static int unsubscribeFlow(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        if (email == null) return 0;

        String url = System.getenv("DB_URL");   // e.g. "jdbc:postgresql://host:5432/appdb?sslmode=require"
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");
        if (url == null || user == null || pass == null) return 0;

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            conn.setReadOnly(true);
            try {
                conn.setNetworkTimeout(null, (int) Duration.ofSeconds(5).toMillis());
            } catch (Exception ignored) {}

            if (!emailExists(conn, email)) return 0;
        } catch (Exception ignored) {
            return 0;
        }

        return runUnsubscribePhp(email) ? 1 : 0;
    }

    public static void main(String[] args) {
        String raw = (args.length > 0) ? Objects.toString(args[0], "") : "";
        int v = unsubscribeFlow(raw);
        System.exit(v == 1 ? 0 : 1);
    }
}
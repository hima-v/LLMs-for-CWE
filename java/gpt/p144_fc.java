// Java (JDBC PostgreSQL + ProcessBuilder) - secure email check + safe PHP invocation
import java.io.IOException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.sql.*;

public class p144_fc {
    private static final Pattern EMAIL_RE = Pattern.compile(
            "^(?=.{3,254}$)[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$"
    );

    private static final String PHP_BIN = "/usr/bin/php";
    private static final String PHP_SCRIPT = "/opt/unsub/unsubscribe.php";
    private static final Duration PHP_TIMEOUT = Duration.ofSeconds(5);

    static String normalizeEmail(String email) {
        if (email == null) return null;
        String e = email.trim().toLowerCase(Locale.ROOT);
        if (e.isEmpty() || e.length() > 254) return null;
        for (int i = 0; i < e.length(); i++) {
            int c = e.charAt(i);
            if (c < 33 || c > 126) return null; // reject spaces/control/non-ascii
        }
        if (!EMAIL_RE.matcher(e).matches()) return null;
        return e;
    }

    static boolean emailExists(Connection conn, String emailNorm) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, emailNorm);
            ps.setQueryTimeout(3);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    static int runPhpUnsubscribe(String emailNorm) {
        ProcessBuilder pb = new ProcessBuilder(PHP_BIN, PHP_SCRIPT, emailNorm);
        pb.redirectErrorStream(true); // avoid blocking on stderr
        try {
            Process p = pb.start();
            boolean finished = p.waitFor(PHP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                p.destroyForcibly();
                return 255;
            }
            return p.exitValue();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return 255;
        }
    }

    // Returns 0 if email not found OR unsubscribe failed; 1 if email found AND PHP returned 0.
    public static int unsubscribe(String jdbcUrl, String user, String pass, String emailInput) {
        String emailNorm = normalizeEmail(emailInput);
        if (emailNorm == null) return 0;

        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, pass)) {
            conn.setNetworkTimeout(Runnable::run, 3000);

            boolean exists = emailExists(conn, emailNorm);
            if (!exists) return 0;

            int phpRc = runPhpUnsubscribe(emailNorm);
            return (phpRc == 0) ? 1 : 0;

        } catch (SQLException e) {
            return 0;
        }
    }

    // Example CLI:
    // java UnsubscribeFlow "jdbc:postgresql://localhost:5432/app" "app" "secret" "user@example.com"
    public static void main(String[] args) {
        if (args.length != 4) System.exit(0);
        int rc = unsubscribe(args[0], args[1], args[2], args[3]);
        System.exit(rc);
    }
}
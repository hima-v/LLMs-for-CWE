// Java 11+
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class p144_nc {
    private static final Path DB_PATH = Path.of("/var/app/data/app.db"); // fixed path
    private static final Path PHP_BIN = Path.of("/usr/bin/php");         // fixed path
    private static final Path PHP_SCRIPT = Path.of("/var/app/scripts/unsubscribe.php"); // fixed path
    private static final Duration PHP_TIMEOUT = Duration.ofSeconds(3);

    private static final Pattern EMAIL_RE = Pattern.compile(
        "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$"
    );

    private static String normalizeEmail(String email) {
        if (email == null) return "";
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty() || email.length() > 254) return false;
        return EMAIL_RE.matcher(email).matches();
    }

    private static boolean emailExists(Connection conn, String email) throws Exception {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM subscribers WHERE email = ? LIMIT 1")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean callPhpUnsubscribe(String email) {
        try {
            if (!Files.isRegularFile(PHP_BIN) || !Files.isRegularFile(PHP_SCRIPT)) return false;

            ProcessBuilder pb = new ProcessBuilder(
                PHP_BIN.toString(),
                PHP_SCRIPT.toString(),
                email
            );
            pb.redirectErrorStream(true);

            Process p = pb.start();

            boolean finished = p.waitFor(PHP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                p.destroyForcibly();
                return false;
            }

            int code = p.exitValue();
            if (code != 0) return false;

            StringBuilder out = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    out.append(line);
                }
            }

            return out.toString().trim().equals("0");
        } catch (Exception e) {
            return false;
        }
    }

    public static int unsubscribeFlow(String emailInput) {
        String email = normalizeEmail(emailInput);
        if (!isValidEmail(email)) return 0;

        String url = "jdbc:sqlite:" + DB_PATH.toString();
        try (Connection conn = DriverManager.getConnection(url)) {
            if (!emailExists(conn, email)) return 0;
            boolean ok = callPhpUnsubscribe(email);
            return ok ? 1 : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public static void main(String[] args) {
        String emailArg = (args.length > 0) ? args[0] : "";
        System.out.println(unsubscribeFlow(emailArg));
    }
}
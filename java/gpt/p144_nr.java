// Java (JDBC + ProcessBuilder, PostgreSQL example)
import java.sql.*;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.regex.Pattern;

public final class p144_nr {
  private static final String PHP_PATH = "/usr/bin/php";
  private static final String PHP_SCRIPT = "/opt/app/unsubscribe.php";
  private static final Duration PHP_TIMEOUT = Duration.ofSeconds(3);

  private static final Pattern EMAIL_RE = Pattern.compile(
      "^[A-Za-z0-9._%+\\-]{1,64}@[A-Za-z0-9\\-]+(?:\\.[A-Za-z0-9\\-]+)+$"
  );

  private static String normalizeEmail(String input) {
    if (input == null) return null;
    String e = input.trim().toLowerCase(Locale.ROOT);
    if (e.length() < 3 || e.length() > 254) return null;
    if (!EMAIL_RE.matcher(e).matches()) return null;
    return e;
  }

  private static int runPhpUnsubscribe(String email) {
    ProcessBuilder pb = new ProcessBuilder(PHP_PATH, PHP_SCRIPT, email);
    pb.redirectInput(ProcessBuilder.Redirect.DISCARD);
    pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
    pb.redirectError(ProcessBuilder.Redirect.DISCARD);

    try {
      Process p = pb.start();
      ExecutorService ex = Executors.newSingleThreadExecutor();
      Future<Integer> f = ex.submit(() -> p.waitFor());
      try {
        return f.get(PHP_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
      } catch (TimeoutException te) {
        p.destroyForcibly();
        return 1;
      } finally {
        ex.shutdownNow();
      }
    } catch (Exception e) {
      return 1;
    }
  }

  // Returns: email not in DB => 0; email in DB and PHP returns 0 => 1; else 0
  public static int unsubscribeFlow(String jdbcUrl, String user, String pass, String inputEmail) {
    String email = normalizeEmail(inputEmail);
    if (email == null) return 0;

    boolean exists = false;
    try (Connection conn = DriverManager.getConnection(jdbcUrl, user, pass);
         PreparedStatement ps = conn.prepareStatement(
             "SELECT 1 FROM subscribers WHERE email = ? LIMIT 1"
         )) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        exists = rs.next();
      }
    } catch (Exception e) {
      return 0;
    }

    if (!exists) return 0;

    int phpRc = runPhpUnsubscribe(email);
    return (phpRc == 0) ? 1 : 0;
  }
}
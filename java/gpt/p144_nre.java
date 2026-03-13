// Java (JDBC + safe PHP exec)
import java.io.File;
import java.sql.*;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public final class p144_nre {
  private static final String DB_URL = "jdbc:sqlite:/opt/app/app.db";
  private static final String PHP_BIN = "/usr/bin/php";
  private static final String PHP_SCRIPT = "/opt/app/unsub.php";
  private static final long PHP_TIMEOUT_MS = 5000;

  private static final Pattern EMAIL_RE = Pattern.compile(
      "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$"
  );

  private static String normalizeEmail(String email) {
    if (email == null) return null;
    String e = email.trim().toLowerCase(Locale.ROOT);
    if (e.isEmpty() || e.length() > 254) return null;
    if (e.indexOf('\0') >= 0) return null;
    if (!EMAIL_RE.matcher(e).matches()) return null;
    int at = e.indexOf('@');
    if (at <= 0 || at != e.lastIndexOf('@')) return null;
    String local = e.substring(0, at);
    String domain = e.substring(at + 1);
    if (local.length() > 64 || domain.length() > 255) return null;
    return e;
  }

  private static boolean emailExists(String email) {
    try (Connection conn = DriverManager.getConnection(DB_URL);
         PreparedStatement ps = conn.prepareStatement(
             "SELECT 1 FROM subscribers WHERE email = ? LIMIT 1"
         )) {
      ps.setString(1, email);
      try (ResultSet rs = ps.executeQuery()) {
        return rs.next();
      }
    } catch (Exception ignored) {
      return false;
    }
  }

  private static int runPhpUnsub(String email) {
    try {
      ProcessBuilder pb = new ProcessBuilder(PHP_BIN, PHP_SCRIPT, email);
      pb.redirectInput(ProcessBuilder.Redirect.from(new File("/dev/null")));
      pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
      pb.redirectError(ProcessBuilder.Redirect.DISCARD);
      Process p = pb.start();
      boolean finished = p.waitFor(PHP_TIMEOUT_MS, TimeUnit.MILLISECONDS);
      if (!finished) {
        p.destroyForcibly();
        return 1;
      }
      return p.exitValue();
    } catch (Exception ignored) {
      return 1;
    }
  }

  public static int processUnsubscribe(String rawEmail) {
    String email = normalizeEmail(rawEmail);
    if (email == null) return 0;

    if (!emailExists(email)) return 0;

    int rc = runPhpUnsub(email);
    return (rc == 0) ? 1 : 0;
  }

  public static void main(String[] args) {
    String arg = (args.length > 0) ? args[0] : "";
    int v = processUnsubscribe(arg);
    System.exit(v == 1 ? 0 : 1);
  }
}
// Java (Javalin + HikariCP + PostgreSQL) — GET /unsubscribe?email=...&token=...
import io.javalin.Javalin;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Pattern;

public class p143_nr_ver3 {
  private static final int EMAIL_MAX_LEN = 254;
  private static final Pattern EMAIL_RE = Pattern.compile("^[A-Z0-9._%+\\-]+@[A-Z0-9.\\-]+\\.[A-Z]{2,63}$", Pattern.CASE_INSENSITIVE);
  private static final String GENERIC_MSG = "If this email was subscribed, it has been unsubscribed.";
  private static final byte[] HMAC_SECRET = System.getenv().getOrDefault("UNSUB_HMAC_SECRET", "replace-with-strong-secret")
      .getBytes(StandardCharsets.UTF_8);

  public static void main(String[] args) {
    DataSource ds = buildDataSource();

    Javalin app = Javalin.create().start(Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")));

    app.get("/unsubscribe", ctx -> {
      String emailNorm = normalizeEmail(ctx.queryParam("email"));
      String token = ctx.queryParam("token");

      if (emailNorm == null || token == null || !verifySignedToken(emailNorm, token)) {
        ctx.status(200).json(new Msg(GENERIC_MSG));
        return;
      }

      long now = Instant.now().getEpochSecond();
      String tokenHash = sha256Hex(token);

      try (Connection c = ds.getConnection()) {
        c.setAutoCommit(false);

        try (PreparedStatement ps = c.prepareStatement(
            "SELECT used_at, expires_at FROM unsubscribe_tokens WHERE token_hash = ? AND email = ? FOR UPDATE")) {
          ps.setString(1, tokenHash);
          ps.setString(2, emailNorm);
          try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
              Long usedAt = (Long) rs.getObject(1);
              Long expAt = (Long) rs.getObject(2);
              boolean expOk = (expAt == null) || (expAt >= now);
              if (usedAt == null && expOk) {
                try (PreparedStatement upd = c.prepareStatement(
                    "UPDATE unsubscribe_tokens SET used_at = ? WHERE token_hash = ? AND email = ? AND used_at IS NULL")) {
                  upd.setLong(1, now);
                  upd.setString(2, tokenHash);
                  upd.setString(3, emailNorm);
                  upd.executeUpdate();
                }
              }
            }
          }
        }

        try (PreparedStatement del = c.prepareStatement("DELETE FROM users WHERE email = ?")) {
          del.setString(1, emailNorm);
          del.executeUpdate();
        }

        c.commit();
      } catch (Exception ignored) {
        // swallow and return generic
      }

      ctx.status(200).json(new Msg(GENERIC_MSG));
    });
  }

  private static String normalizeEmail(String raw) {
    if (raw == null) return null;
    String e = raw.trim().toLowerCase();
    if (e.isEmpty() || e.length() > EMAIL_MAX_LEN) return null;
    if (!EMAIL_RE.matcher(e).matches()) return null;
    return e;
  }

  private static boolean verifySignedToken(String emailNorm, String token) {
    String[] parts = token.trim().split("\\.");
    if (parts.length != 2) return false;
    String emailB64 = parts[0];
    String sigB64 = parts[1];

    byte[] sig;
    String tokenEmail;
    try {
      tokenEmail = new String(Base64.getUrlDecoder().decode(emailB64), StandardCharsets.UTF_8);
      sig = Base64.getUrlDecoder().decode(sigB64);
    } catch (Exception e) {
      return false;
    }
    if (!tokenEmail.equals(emailNorm)) return false;

    byte[] mac = hmacSha256(emailB64.getBytes(StandardCharsets.UTF_8), HMAC_SECRET);
    return MessageDigest.isEqual(mac, sig);
  }

  private static byte[] hmacSha256(byte[] msg, byte[] secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return mac.doFinal(msg);
    } catch (Exception e) {
      return new byte[0];
    }
  }

  private static String sha256Hex(String s) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] dig = md.digest(s.getBytes(StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(dig.length * 2);
      for (byte b : dig) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      return "";
    }
  }

  private static DataSource buildDataSource() {
    com.zaxxer.hikari.HikariConfig cfg = new com.zaxxer.hikari.HikariConfig();
    cfg.setJdbcUrl(System.getenv().getOrDefault("JDBC_URL", "jdbc:postgresql://localhost:5432/app"));
    cfg.setUsername(System.getenv().getOrDefault("DB_USER", "app"));
    cfg.setPassword(System.getenv().getOrDefault("DB_PASS", "app"));
    cfg.setMaximumPoolSize(10);
    return new com.zaxxer.hikari.HikariDataSource(cfg);
  }

  static class Msg {
    public final String message;
    Msg(String message) { this.message = message; }
  }
}
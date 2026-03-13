// Java (Spring Boot + JdbcTemplate) — GET /unsubscribe?email=...&token=...
package com.example.unsub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

@SpringBootApplication
public class p143_nr_ver2 {
  public static void main(String[] args) {
    SpringApplication.run(UnsubApp.class, args);
  }
}

@RestController
class UnsubController {
  private static final int EMAIL_MAX_LEN = 254;
  private static final Pattern EMAIL_RE = Pattern.compile("^[A-Z0-9._%+\\-]+@[A-Z0-9.\\-]+\\.[A-Z]{2,63}$", Pattern.CASE_INSENSITIVE);
  private static final String GENERIC_MSG = "If this email was subscribed, it has been unsubscribed.";

  private final JdbcTemplate jdbc;
  private final byte[] hmacSecret;

  UnsubController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
    String secret = System.getenv().getOrDefault("UNSUB_HMAC_SECRET", "replace-with-strong-secret");
    this.hmacSecret = secret.getBytes(StandardCharsets.UTF_8);
  }

  @GetMapping("/unsubscribe")
  public ResponseEntity<?> unsubscribe(@RequestParam(value = "email", required = false) String email,
                                       @RequestParam(value = "token", required = false) String token) {
    String emailNorm = normalizeEmail(email);
    if (emailNorm == null || token == null || !verifySignedToken(emailNorm, token)) {
      return ResponseEntity.ok(new Msg(GENERIC_MSG));
    }

    long now = System.currentTimeMillis() / 1000L;
    String tokenHash = sha256Hex(token);

    try {
      jdbc.execute("BEGIN");

      List<TokenRow> rows = jdbc.query(
          "SELECT used_at, expires_at FROM unsubscribe_tokens WHERE token_hash = ? AND email = ? FOR UPDATE",
          (rs, n) -> new TokenRow(rs.getObject("used_at") == null ? null : rs.getLong("used_at"),
                                 rs.getObject("expires_at") == null ? null : rs.getLong("expires_at")),
          tokenHash, emailNorm
      );

      if (rows.size() == 1) {
        TokenRow r = rows.get(0);
        boolean expOk = (r.expiresAt == null) || (r.expiresAt >= now);
        if (r.usedAt == null && expOk) {
          jdbc.update("UPDATE unsubscribe_tokens SET used_at = ? WHERE token_hash = ? AND email = ? AND used_at IS NULL",
              now, tokenHash, emailNorm);
        }
      }

      jdbc.update("DELETE FROM users WHERE email = ?", emailNorm);
      jdbc.execute("COMMIT");
    } catch (Exception ignored) {
      try { jdbc.execute("ROLLBACK"); } catch (Exception ignored2) {}
    }

    return ResponseEntity.ok(new Msg(GENERIC_MSG));
  }

  private String normalizeEmail(String raw) {
    if (raw == null) return null;
    String e = raw.trim().toLowerCase();
    if (e.isEmpty() || e.length() > EMAIL_MAX_LEN) return null;
    if (!EMAIL_RE.matcher(e).matches()) return null;
    return e;
  }

  private boolean verifySignedToken(String emailNorm, String token) {
    String[] parts = token.trim().split("\\.");
    if (parts.length != 2) return false;
    String emailB64 = parts[0];
    String sigB64 = parts[1];

    String tokenEmail;
    byte[] sig;
    try {
      tokenEmail = new String(Base64.getUrlDecoder().decode(emailB64), StandardCharsets.UTF_8);
      sig = Base64.getUrlDecoder().decode(sigB64);
    } catch (Exception e) {
      return false;
    }
    if (!tokenEmail.equals(emailNorm)) return false;

    byte[] mac = hmacSha256(emailB64.getBytes(StandardCharsets.UTF_8), hmacSecret);
    return MessageDigest.isEqual(mac, sig);
  }

  private byte[] hmacSha256(byte[] msg, byte[] secret) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return mac.doFinal(msg);
    } catch (Exception e) {
      return new byte[0];
    }
  }

  private String sha256Hex(String s) {
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

  static class Msg {
    public final String message;
    Msg(String message) { this.message = message; }
  }

  static class TokenRow {
    final Long usedAt;
    final Long expiresAt;
    TokenRow(Long usedAt, Long expiresAt) { this.usedAt = usedAt; this.expiresAt = expiresAt; }
  }
}
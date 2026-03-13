import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class p143_nre {

  private static final String GENERIC_MSG = "If this email was subscribed, it has been unsubscribed.";
  private static final Pattern EMAIL_RE = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  private final JdbcTemplate jdbc;
  private final byte[] secret;

  public p143_nre(DataSource ds) {
    this.jdbc = new JdbcTemplate(ds);
    this.secret = System.getenv().getOrDefault("UNSUB_SECRET", "replace-with-strong-secret")
        .getBytes(StandardCharsets.UTF_8);
  }

  private static String normalizeEmail(String raw) {
    if (raw == null) return null;
    String s = raw.trim();
    if (s.length() < 1 || s.length() > 254) return null;
    if (!EMAIL_RE.matcher(s).matches()) return null;
    int at = s.lastIndexOf('@');
    if (at <= 0 || at >= s.length() - 3) return null;
    String local = s.substring(0, at);
    String domain = s.substring(at + 1).toLowerCase();
    return local + "@" + domain;
  }

  private static byte[] b64urlDecode(String s) {
    String str = s.replace('-', '+').replace('_', '/');
    int pad = (4 - (str.length() % 4)) % 4;
    str = str + "=".repeat(pad);
    return java.util.Base64.getDecoder().decode(str);
  }

  private static boolean constantTimeEquals(byte[] a, byte[] b) {
    return MessageDigest.isEqual(a, b);
  }

  private String verifyToken(String token) {
    try {
      String[] parts = token.split("\\.", 2);
      if (parts.length != 2) return null;

      byte[] payload = b64urlDecode(parts[0]);
      byte[] sig = b64urlDecode(parts[1]);

      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      byte[] expected = mac.doFinal(payload);

      if (sig.length != expected.length || !constantTimeEquals(sig, expected)) return null;

      Map<String, String> kv = parseQuery(new String(payload, StandardCharsets.UTF_8));
      long exp = Long.parseLong(kv.getOrDefault("exp", "0"));
      if (exp <= Instant.now().getEpochSecond()) return null;

      String email = normalizeEmail(kv.get("email"));
      if (email == null) return null;

      // Optional: enforce nonce single-use: kv.get("nonce")
      return email;
    } catch (Exception e) {
      return null;
    }
  }

  private static Map<String, String> parseQuery(String qs) {
    Map<String, String> m = new HashMap<>();
    String[] parts = qs.split("&");
    for (String p : parts) {
      int i = p.indexOf('=');
      if (i > 0 && i < p.length() - 1) {
        m.put(p.substring(0, i), p.substring(i + 1));
      }
    }
    return m;
  }

  private void mailinglistUnsubscribe(String email) {
    // Idempotent external call placeholder
  }

  @GetMapping(value = "/unsubscribe", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> unsubscribe(@RequestParam(name = "token", required = false) String token,
                                            HttpServletRequest req) {
    String email = verifyToken(token == null ? "" : token);

    if (email != null) {
      try {
        jdbc.update("DELETE FROM users WHERE email = ?", email); // parameterized
        mailinglistUnsubscribe(email);
      } catch (Exception ignored) {
      }
    }

    return ResponseEntity.ok(GENERIC_MSG);
  }
}
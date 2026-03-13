// java (Spring Boot) - secure unsubscribe via HMAC-signed token (GET)
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Pattern;

@SpringBootApplication
public class p143_nc {
  public static void main(String[] args) {
    SpringApplication.run(UnsubApp.class, args);
  }
}

@RestController
class p143_nc {
  private static final String GENERIC_MESSAGE = "If the address is subscribed, it has been unsubscribed.";
  private static final byte[] SECRET = "CHANGE_ME_TO_A_LONG_RANDOM_SECRET".getBytes(StandardCharsets.UTF_8);
  private static final Pattern EMAIL_RE = Pattern.compile("^[A-Z0-9._%+\\-]+@[A-Z0-9.\\-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

  private final JdbcTemplate jdbc;

  p143_nc(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @GetMapping("/unsubscribe")
  public ResponseEntity<Map<String, String>> unsubscribe(@RequestParam(name = "t", required = false) String token) {
    String email;
    try {
      Map<String, Object> data = verifyToken(token, 7 * 24 * 3600);
      email = (String) data.get("email");
    } catch (Exception e) {
      return ResponseEntity.ok(Map.of("message", GENERIC_MESSAGE));
    }

    boolean existed = false;
    try {
      Integer one = jdbc.query("SELECT 1 FROM users WHERE email = ?",
          ps -> ps.setString(1, email),
          rs -> rs.next() ? 1 : null);
      existed = (one != null);

      if (existed) {
        jdbc.update("DELETE FROM users WHERE email = ?", email);
      }
    } catch (Exception e) {
      return ResponseEntity.ok(Map.of("message", GENERIC_MESSAGE));
    }

    if (existed) {
      mailingListUnsubscribe(email);
    }

    return ResponseEntity.ok(Map.of("message", GENERIC_MESSAGE));
  }

  private static String normalizeEmail(String email) {
    email = email == null ? "" : email.trim();
    if (email.isEmpty() || email.length() > 254) throw new IllegalArgumentException("invalid");
    email = email.toLowerCase();
    if (!EMAIL_RE.matcher(email).matches()) throw new IllegalArgumentException("invalid");
    return email;
  }

  private static String b64urlEncode(byte[] b) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }

  private static byte[] b64urlDecode(String s) {
    if (s == null) throw new IllegalArgumentException("invalid");
    return Base64.getUrlDecoder().decode(s);
  }

  private static String hmacSha256(byte[] key, byte[] msg) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(key, "HmacSHA256"));
    return b64urlEncode(mac.doFinal(msg));
  }

  private static boolean constantTimeEquals(String a, String b) {
    if (a == null || b == null) return false;
    byte[] ab = a.getBytes(StandardCharsets.UTF_8);
    byte[] bb = b.getBytes(StandardCharsets.UTF_8);
    if (ab.length != bb.length) return false;
    int r = 0;
    for (int i = 0; i < ab.length; i++) r |= ab[i] ^ bb[i];
    return r == 0;
  }

  // Token format: base64url( JSON {"email": "...", "exp": 123, "sig": "..."} )
  // Signature is HMAC over JSON {"email":"...","exp":...} with sorted keys and compact separators.
  @SuppressWarnings("unchecked")
  private static Map<String, Object> verifyToken(String token, int maxAgeSeconds) throws Exception {
    String json = new String(b64urlDecode(token), StandardCharsets.UTF_8);

    // Minimal JSON parsing without extra deps (expects exact keys).
    // For production, use Jackson with strict schema validation.
    String email = extractJsonString(json, "email");
    long exp = Long.parseLong(extractJsonNumber(json, "exp"));
    String sig = extractJsonString(json, "sig");

    email = normalizeEmail(email);

    long now = Instant.now().getEpochSecond();
    if (exp < now || exp > now + maxAgeSeconds) throw new IllegalArgumentException("expired");

    String unsigned = "{\"email\":\"" + email.replace("\\", "\\\\").replace("\"", "\\\"") + "\",\"exp\":" + exp + "}";
    String expected = hmacSha256(SECRET, unsigned.getBytes(StandardCharsets.UTF_8));
    if (!constantTimeEquals(expected, sig)) throw new IllegalArgumentException("bad sig");

    return Map.of("email", email, "exp", exp);
  }

  private static String extractJsonString(String json, String key) {
    String k = "\"" + key + "\":";
    int i = json.indexOf(k);
    if (i < 0) throw new IllegalArgumentException("missing");
    int start = json.indexOf('"', i + k.length());
    int end = json.indexOf('"', start + 1);
    if (start < 0 || end < 0) throw new IllegalArgumentException("bad");
    return json.substring(start + 1, end);
  }

  private static String extractJsonNumber(String json, String key) {
    String k = "\"" + key + "\":";
    int i = json.indexOf(k);
    if (i < 0) throw new IllegalArgumentException("missing");
    int start = i + k.length();
    while (start < json.length() && (json.charAt(start) == ' ')) start++;
    int end = start;
    while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
    if (end == start) throw new IllegalArgumentException("bad");
    return json.substring(start, end);
  }

  private static void mailingListUnsubscribe(String email) {
    // Placeholder for ESP integration; keep idempotent.
  }

  // helper: server-side token creation
  static String makeUnsubToken(String email, int ttlSeconds) throws Exception {
    email = normalizeEmail(email);
    long exp = Instant.now().getEpochSecond() + ttlSeconds;
    String unsigned = "{\"email\":\"" + email.replace("\\", "\\\\").replace("\"", "\\\"") + "\",\"exp\":" + exp + "}";
    String sig = hmacSha256(SECRET, unsigned.getBytes(StandardCharsets.UTF_8));
    String tokenJson = "{\"email\":\"" + email.replace("\\", "\\\\").replace("\"", "\\\"") + "\",\"exp\":" + exp + ",\"sig\":\"" + sig + "\"}";
    return b64urlEncode(tokenJson.getBytes(StandardCharsets.UTF_8));
  }
}
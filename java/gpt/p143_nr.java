import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class p143_nr {

    private static final Pattern EMAIL_RE = Pattern.compile("^[A-Z0-9._%+\\-']+@[A-Z0-9.\\-]+\\.[A-Z]{2,63}$",
            Pattern.CASE_INSENSITIVE);
    private static final int MAX_EMAIL_LEN = 254;

    private static final String GENERIC_MESSAGE = "If this address is subscribed, it has been unsubscribed.";

    private final DataSource dataSource;
    private final String secret;

    public p143_nr(DataSource dataSource) {
        this.dataSource = dataSource;
        this.secret = System.getenv().getOrDefault("UNSUB_SECRET", "");
    }

    private static String normalizeEmail(String email) {
        if (email == null) return null;
        String e = email.trim();
        if (e.isEmpty() || e.length() > MAX_EMAIL_LEN) return null;
        if (!EMAIL_RE.matcher(e).matches()) return null;
        int at = e.indexOf('@');
        if (at <= 0) return null;
        String local = e.substring(0, at).trim();
        String domain = e.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        return local + "@" + domain;
    }

    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a == null || b == null || a.length != b.length) return false;
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= (a[i] ^ b[i]);
        }
        return result == 0;
    }

    private boolean verifyToken(String email, String tokenHex) {
        if (secret == null || secret.isEmpty() || tokenHex == null || tokenHex.isEmpty()) return false;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(email.getBytes(StandardCharsets.UTF_8));
            byte[] provided = hexToBytes(tokenHex);
            return constantTimeEquals(digest, provided);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        if (len % 2 != 0) return new byte[0];
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            if (hi < 0 || lo < 0) return new byte[0];
            out[i / 2] = (byte) ((hi << 4) + lo);
        }
        return out;
    }

    private void mailingListUnsubscribe(String _email) {
        // Integrate with your ESP using server-side API + proper auth.
    }

    @GetMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestParam(value = "email", required = false) String emailRaw,
                                         @RequestParam(value = "token", required = false) String token) {
        String email = normalizeEmail(emailRaw);

        if (email == null || !verifyToken(email, token)) {
            return ResponseEntity.ok().body(new Message(GENERIC_MESSAGE));
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE email = ?")) {
            ps.setString(1, email);
            ps.executeUpdate();
            mailingListUnsubscribe(email);
        } catch (Exception ignored) {
            // Intentionally generic response.
        }

        return ResponseEntity.ok().body(new Message(GENERIC_MESSAGE));
    }

    public record Message(String message) {}
}
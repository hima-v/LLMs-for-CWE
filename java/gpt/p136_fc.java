// Java (Spring Boot)
// WARNING: Sending credentials in URL query params is insecure (URLs can be logged in many places).
// Assume HTTPS, and prefer POST + body + proper auth schemes in real systems.

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class p136_fc {
  public static void main(String[] args) {
    SpringApplication.run(p136_fc.class, args);
  }
}

@RestController
class CheckModController {

  // Stored reference (prefer a real secrets manager). Example envs:
  //   AUTH_USER=admin
  //   AUTH_PW_SALT_HEX=001122...
  //   AUTH_PW_PBKDF2_HEX=aabbcc... (derived key hex)
  private static final String AUTH_USER = getenvOrDefault("AUTH_USER", "admin");
  private static final byte[] AUTH_PW_SALT = hexToBytes(getenvOrDefault("AUTH_PW_SALT_HEX", repeat("00", 16)));
  private static final byte[] AUTH_PW_DK = hexToBytes(getenvOrDefault("AUTH_PW_PBKDF2_HEX", repeat("00", 32)));
  private static final int PBKDF2_ITERS = Integer.parseInt(getenvOrDefault("AUTH_PBKDF2_ITERS", "200000"));

  // Basic per-IP sliding-window rate limit
  private static final long WINDOW_SEC = Long.parseLong(getenvOrDefault("RL_WINDOW_SEC", "60"));
  private static final int MAX_REQS = Integer.parseInt(getenvOrDefault("RL_MAX_REQS", "30"));
  private static final Map<String, Deque<Long>> REQ_LOG = new ConcurrentHashMap<>();

  @GetMapping(value = "/check_mod", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> checkMod(
      @RequestParam(name = "username", required = false, defaultValue = "") String username,
      @RequestParam(name = "password", required = false, defaultValue = "") String password,
      jakarta.servlet.http.HttpServletRequest request
  ) {
    String ip = clientIp(request);

    if (rateLimited(ip)) {
      return ResponseEntity.status(429).contentType(MediaType.TEXT_PLAIN).body("false");
    }

    // Constant-time comparisons:
    // - Compare usernames in constant time too (avoid user enumeration timing side-channels).
    // - Always run PBKDF2 to equalize timing even if username is wrong.
    boolean userOk = MessageDigest.isEqual(
        username.getBytes(StandardCharsets.UTF_8),
        AUTH_USER.getBytes(StandardCharsets.UTF_8)
    );

    byte[] dk;
    try {
      dk = pbkdf2(password.toCharArray(), AUTH_PW_SALT, PBKDF2_ITERS, AUTH_PW_DK.length);
    } catch (Exception e) {
      dk = new byte[AUTH_PW_DK.length];
    }

    boolean pwOk = MessageDigest.isEqual(dk, AUTH_PW_DK);

    return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body((userOk && pwOk) ? "true" : "false");
  }

  private static boolean rateLimited(String ip) {
    long now = Instant.now().getEpochSecond();
    long cutoff = now - WINDOW_SEC;

    Deque<Long> q = REQ_LOG.computeIfAbsent(ip, k -> new ArrayDeque<>());
    synchronized (q) {
      while (!q.isEmpty() && q.peekFirst() < cutoff) q.pollFirst();
      if (q.size() >= MAX_REQS) return true;
      q.addLast(now);
      return false;
    }
  }

  private static String clientIp(jakarta.servlet.http.HttpServletRequest req) {
    String xff = req.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
    return (req.getRemoteAddr() != null) ? req.getRemoteAddr() : "unknown";
  }

  private static byte[] pbkdf2(char[] password, byte[] salt, int iters, int dkLen) throws Exception {
    PBEKeySpec spec = new PBEKeySpec(password, salt, iters, dkLen * 8);
    SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    return skf.generateSecret(spec).getEncoded();
  }

  private static String getenvOrDefault(String k, String def) {
    String v = System.getenv(k);
    return (v == null || v.isBlank()) ? def : v;
  }

  private static byte[] hexToBytes(String hex) {
    int len = hex.length();
    if ((len & 1) == 1) throw new IllegalArgumentException("hex length must be even");
    byte[] out = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      out[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
    }
    return out;
  }

  private static String repeat(String s, int n) {
    StringBuilder sb = new StringBuilder(s.length() * n);
    for (int i = 0; i < n; i++) sb.append(s);
    return sb.toString();
  }
}
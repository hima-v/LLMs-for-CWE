// Java (Spring Boot)
// NOTE: Demo uses URL query params; in real systems use POST + HTTPS (avoid secrets in URLs/logs).

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

@SpringBootApplication
public class p136_nr_ver2 {
  public static void main(String[] args) {
    SpringApplication.run(CheckModApp.class, args);
  }
}

@RestController
class CheckModController {

  private static final class Cred {
    final byte[] salt;
    final byte[] dk;
    Cred(byte[] salt, byte[] dk) { this.salt = salt; this.dk = dk; }
  }

  // Demo credential store: username -> salt + PBKDF2 hash
  private static final Map<String, Cred> USERS = new ConcurrentHashMap<>();
  private static final int ITER = 200_000;
  private static final int DK_LEN_BITS = 256;

  // Dummy values to equalize work when user is missing (reduces user-enumeration timing leaks)
  private static final byte[] DUMMY_SALT = "demo_salt_dummy____".getBytes(StandardCharsets.UTF_8);
  private static final byte[] DUMMY_DK = pbkdf2("dummy_password".toCharArray(), DUMMY_SALT, ITER, DK_LEN_BITS);

  // Basic per-IP throttling
  private static final long WINDOW_SECONDS = 60;
  private static final int MAX_ATTEMPTS = 10;
  private static final Map<String, ArrayDeque<Long>> ATTEMPTS = new ConcurrentHashMap<>();

  static {
    byte[] salt = "demo_salt_alice".getBytes(StandardCharsets.UTF_8);
    byte[] dk = pbkdf2("correcthorsebatterystaple".toCharArray(), salt, ITER, DK_LEN_BITS);
    USERS.put("alice", new Cred(salt, dk));
  }

  private static boolean allow(String ip) {
    long now = Instant.now().getEpochSecond();
    ArrayDeque<Long> q = ATTEMPTS.computeIfAbsent(ip, k -> new ArrayDeque<>());
    synchronized (q) {
      while (!q.isEmpty() && now - q.peekFirst() >= WINDOW_SECONDS) q.removeFirst();
      if (q.size() >= MAX_ATTEMPTS) return false;
      q.addLast(now);
      return true;
    }
  }

  private static byte[] pbkdf2(char[] password, byte[] salt, int iter, int dkLenBits) {
    try {
      PBEKeySpec spec = new PBEKeySpec(password, salt, iter, dkLenBits);
      SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      return skf.generateSecret(spec).getEncoded();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @GetMapping(value = "/check_mod", produces = MediaType.TEXT_PLAIN_VALUE)
  public String checkMod(
      @RequestParam(name = "username", defaultValue = "") String username,
      @RequestParam(name = "password", defaultValue = "") String password,
      HttpServletRequest req
  ) {
    String ip = req.getHeader("X-Forwarded-For");
    if (ip == null || ip.isBlank()) ip = req.getRemoteAddr();
    if (ip == null || ip.isBlank()) ip = "unknown";
    ip = ip.split(",")[0].trim();

    if (!allow(ip)) return "false";

    Cred rec = USERS.get(username);
    byte[] salt = (rec != null) ? rec.salt : DUMMY_SALT;
    byte[] expected = (rec != null) ? rec.dk : DUMMY_DK;

    byte[] provided = pbkdf2(password.toCharArray(), salt, ITER, expected.length * 8);

    boolean sameLen = provided.length == expected.length;
    boolean ok = MessageDigest.isEqual(sameLen ? provided : new byte[expected.length], expected);

    return ok ? "true" : "false";
  }
}
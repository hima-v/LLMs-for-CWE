// java (Spring Boot)
// NOTE: This is a constrained demo using URL query params. In real systems, use POST + HTTPS (and avoid credentials in URLs/logs).

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@SpringBootApplication
public class p136_nr {
  public static void main(String[] args) {
    SpringApplication.run(p136_nr.class, args);
  }
}

@RestController
class CheckModController {

  // ----- Simple in-memory rate limiting (per IP) -----
  private static final long WINDOW_SECONDS = 60;
  private static final int MAX_ATTEMPTS = 10;
  private final Map<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

  private boolean isRateLimited(String ip) {
    long now = Instant.now().getEpochSecond();
    Deque<Long> q = attempts.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
    while (true) {
      Long head = q.peekFirst();
      if (head == null) break;
      if ((now - head) > WINDOW_SECONDS) q.pollFirst();
      else break;
    }
    if (q.size() >= MAX_ATTEMPTS) return true;
    q.addLast(now);
    return false;
  }

  // ----- Password hashing (PBKDF2) + constant-time checks -----
  private static final int PBKDF2_ITERS = 200_000;
  private static final int DK_LEN_BITS = 256;
  private static final SecureRandom RNG = new SecureRandom();

  private byte[] pbkdf2(String password, byte[] salt) {
    try {
      PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERS, DK_LEN_BITS);
      SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      return skf.generateSecret(spec).getEncoded();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  static class UserRec {
    final byte[] salt;
    final byte[] dk;
    UserRec(byte[] salt, byte[] dk) {
      this.salt = salt;
      this.dk = dk;
    }
  }

  private final Map<String, UserRec> userDb = new ConcurrentHashMap<>();
  private final byte[] dummySalt = new byte[16];
  private final byte[] dummyDk;

  public CheckModController() {
    Arrays.fill(dummySalt, (byte)0);
    dummyDk = pbkdf2("dummy-password", dummySalt);
    addUser("alice", "CorrectHorseBatteryStaple!");
  }

  private void addUser(String username, String password) {
    byte[] salt = new byte[16];
    RNG.nextBytes(salt);
    byte[] dk = pbkdf2(password, salt);
    userDb.put(username, new UserRec(salt, dk));
  }

  @GetMapping(value = "/check_mod", produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> checkMod(
      @RequestParam(name = "username", defaultValue = "") String username,
      @RequestParam(name = "password", defaultValue = "") String password,
      @RequestHeader(value = "X-Forwarded-For", required = false) String xff,
      @RequestHeader(value = "X-Real-IP", required = false) String xri
  ) {
    String ip = "unknown";
    if (xff != null && !xff.isBlank()) ip = xff.split(",")[0].trim();
    else if (xri != null && !xri.isBlank()) ip = xri.trim();

    if (isRateLimited(ip)) {
      try { Thread.sleep(250); } catch (InterruptedException ignored) {}
      return ResponseEntity.ok("false");
    }

    UserRec rec = userDb.get(username);
    byte[] salt = (rec != null) ? rec.salt : dummySalt;
    byte[] expected = (rec != null) ? rec.dk : dummyDk;

    byte[] provided = pbkdf2(password, salt);

    boolean same = MessageDigest.isEqual(provided, expected);
    boolean ok = same && userDb.containsKey(username);

    return ResponseEntity.ok(ok ? "true" : "false");
  }
}
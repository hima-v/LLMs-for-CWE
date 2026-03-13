// java (Spring Boot)
// NOTE: Demo uses URL params as requested. In real systems, use POST + HTTPS; never send credentials in URLs (they get logged).
// Build: Spring Boot 3.x (Jakarta). This is a single-file example for brevity.
package com.example.checkmod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@SpringBootApplication
public class p136_nre {
  public static void main(String[] args) {
    SpringApplication.run(p136_nre.class, args);
  }
}

@RestController
class CheckModController {
  // ---- Demo credentials (store hashes in real systems) ----
  private static final String DEMO_USER = "admin";
  private static final byte[] SALT = hex("c0ffeec0ffeec0ffeec0ffeec0ffeec0"); // fixed for demo; per-user random salt in real systems
  private static final int ITERATIONS = 200_000;
  private static final int KEYLEN_BITS = 256;

  private static final byte[] DEMO_PW_HASH = pbkdf2("correcthorsebatterystaple".toCharArray(), SALT, ITERATIONS, KEYLEN_BITS);

  // ---- Basic in-memory rate limiting (per-IP sliding window) ----
  private static final long WINDOW_MS = 60_000;
  private static final int MAX_ATTEMPTS = 20;
  private static final Map<String, Deque<Long>> ATTEMPTS = new ConcurrentHashMap<>();

  @GetMapping(value = "/check_mod", produces = MediaType.TEXT_PLAIN_VALUE)
  public String checkMod(
      @RequestParam(name = "username", required = false, defaultValue = "") String username,
      @RequestParam(name = "password", required = false, defaultValue = "") String password,
      HttpServletRequest request
  ) {
    String ip = clientIp(request);
    if (rateLimited(ip)) return "false";

    return authOk(username, password) ? "true" : "false";
  }

  private static String clientIp(HttpServletRequest req) {
    // If behind a trusted proxy, validate and use X-Forwarded-For properly; otherwise keep remote address
    String xff = req.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
    String ra = req.getRemoteAddr();
    return (ra == null || ra.isBlank()) ? "unknown" : ra;
  }

  private static boolean rateLimited(String ip) {
    long now = Instant.now().toEpochMilli();
    Deque<Long> q = ATTEMPTS.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
    while (true) {
      Long head = q.peekFirst();
      if (head == null) break;
      if (now - head > WINDOW_MS) q.pollFirst();
      else break;
    }
    if (q.size() >= MAX_ATTEMPTS) return true;
    q.addLast(now);
    return false;
  }

  private static boolean authOk(String username, String password) {
    // Constant-time compares for both username and password hash
    byte[] userA = (username == null ? "" : username).getBytes(StandardCharsets.UTF_8);
    byte[] userB = DEMO_USER.getBytes(StandardCharsets.UTF_8);
    boolean userOk = MessageDigest.isEqual(userA, userB);

    byte[] pwHash = pbkdf2((password == null ? "" : password).toCharArray(), SALT, ITERATIONS, KEYLEN_BITS);
    boolean pwOk = MessageDigest.isEqual(pwHash, DEMO_PW_HASH);

    // Always compute both checks; generic response only.
    return userOk && pwOk;
  }

  private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLenBits) {
    try {
      PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLenBits);
      SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
      byte[] out = skf.generateSecret(spec).getEncoded();
      Arrays.fill(password, '\0');
      spec.clearPassword();
      return out;
    } catch (Exception e) {
      // In production, handle errors appropriately; keep response generic.
      return new byte[keyLenBits / 8];
    }
  }

  private static byte[] hex(String s) {
    int len = s.length();
    byte[] out = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      out[i / 2] = (byte) Integer.parseInt(s.substring(i, i + 2), 16);
    }
    return out;
  }
}
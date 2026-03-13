// Java (Spring Boot)
// NOTE: In real systems, use POST (not URL/query params) + HTTPS to avoid credential leakage via logs, caches, referrers.

package com.example.checkmod;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class p136_nc {
    public static void main(String[] args) {
        SpringApplication.run(p136_nc.class, args);
    }
}

@RestController
class CheckModController {

    private static final String EXPECTED_USERNAME = "admin";
    private static final byte[] SALT = "static-demo-salt-change-me".getBytes(StandardCharsets.UTF_8);
    private static final int ITERATIONS = 200_000;
    private static final int DKLEN_BITS = 256; // 32 bytes

    // Demo expected password = "password123"
    private static final byte[] EXPECTED_PW_DK = pbkdf2("password123".toCharArray(), SALT, ITERATIONS, DKLEN_BITS);

    // Simple in-memory rate limiter: 5 requests per 60 seconds per IP
    private static final long WINDOW_SEC = 60;
    private static final int MAX_REQ = 5;
    private static final ConcurrentHashMap<String, Deque<Long>> HITS = new ConcurrentHashMap<>();

    private static boolean rateLimited(String ip) {
        long now = Instant.now().getEpochSecond();
        Deque<Long> q = HITS.computeIfAbsent(ip, k -> new ArrayDeque<>());
        synchronized (q) {
            while (!q.isEmpty() && now - q.peekFirst() > WINDOW_SEC) q.pollFirst();
            if (q.size() >= MAX_REQ) return true;
            q.addLast(now);
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int dkLenBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, dkLenBits);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean constantTimeEqualsBytes(byte[] a, byte[] b) {
        return MessageDigest.isEqual(a, b);
    }

    private static boolean constantTimeEqualsString(String a, String b) {
        // Compare byte arrays in constant time; also ensure lengths match.
        byte[] A = a.getBytes(StandardCharsets.UTF_8);
        byte[] B = b.getBytes(StandardCharsets.UTF_8);

        int len = Math.max(Math.max(A.length, B.length), 1);
        byte[] aP = new byte[len];
        byte[] bP = new byte[len];
        System.arraycopy(A, 0, aP, 0, A.length);
        System.arraycopy(B, 0, bP, 0, B.length);

        boolean eq = MessageDigest.isEqual(aP, bP);
        return eq && A.length == B.length;
    }

    @GetMapping(value = "/check_mod", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> checkMod(
            @RequestParam(name = "username", defaultValue = "") String username,
            @RequestParam(name = "password", defaultValue = "") String password,
            HttpServletRequest request
    ) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        if (ip == null) ip = "unknown";
        ip = ip.split(",")[0].trim();

        if (rateLimited(ip)) {
            return ResponseEntity.status(429).body("false");
        }

        // Constant-time comparisons (avoid leaking which field was wrong)
        boolean userOk = constantTimeEqualsString(username, EXPECTED_USERNAME);

        byte[] pwDk = pbkdf2(password.toCharArray(), SALT, ITERATIONS, DKLEN_BITS);
        boolean pwOk = constantTimeEqualsBytes(pwDk, EXPECTED_PW_DK);

        boolean ok = userOk && pwOk;
        return ResponseEntity.status(ok ? 200 : 401).body(ok ? "true" : "false");
    }
}
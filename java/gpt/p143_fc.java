// Java (Spring Boot) — GET /unsubscribe with one-time signed token, JDBC prepared statements, no enumeration
// build.gradle deps: spring-boot-starter-web, spring-boot-starter-jdbc, postgresql
package com.example.unsubscribe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;

@SpringBootApplication
public class p143_fc {
  public static void main(String[] args) {
    SpringApplication.run(p143_fc.class, args);
  }

  @Bean
  public Boolean initSchema(JdbcTemplate jdbc) {
    jdbc.execute("""
      CREATE TABLE IF NOT EXISTS unsubscribe_tokens (
        token_hash BYTEA PRIMARY KEY,
        email TEXT NOT NULL,
        expires_at TIMESTAMPTZ NOT NULL,
        used_at TIMESTAMPTZ
      );
      """);
    jdbc.execute("""
      CREATE TABLE IF NOT EXISTS subscribers (
        email TEXT PRIMARY KEY,
        subscribed BOOLEAN NOT NULL DEFAULT TRUE
      );
      """);
    return true;
  }
}

@RestController
class UnsubscribeController {

  private final JdbcTemplate jdbc;
  private final byte[] secret;

  UnsubscribeController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
    String s = System.getenv().getOrDefault("TOKEN_HMAC_SECRET", "change-me-please");
    this.secret = s.getBytes(StandardCharsets.UTF_8);
  }

  private static String normalizeEmail(String email) {
    return email == null ? "" : email.trim().toLowerCase();
  }

  private static byte[] sha256(String raw) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return md.digest(raw.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private byte[] hmacSha256(byte[] msg) throws Exception {
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(secret, "HmacSHA256"));
    return mac.doFinal(msg);
  }

  private String verifySignedToken(String signedToken) {
    // token format: base64url(raw).base64url(sig)
    try {
      if (signedToken == null || signedToken.length() < 10 || signedToken.length() > 2000) return null;
      String[] parts = signedToken.split("\\.", 2);
      if (parts.length != 2) return null;

      Base64.Decoder urlDec = Base64.getUrlDecoder();
      byte[] raw = urlDec.decode(parts[0]);
      byte[] sig = urlDec.decode(parts[1]);

      byte[] expected = hmacSha256(raw);
      if (sig.length != expected.length) return null;
      if (!MessageDigest.isEqual(sig, expected)) return null;

      return new String(raw, StandardCharsets.UTF_8);
    } catch (Exception e) {
      return null;
    }
  }

  private void mailingListUnsubscribe(String email) {
    // Stub: call ESP/mailing list API. Don't leak outcomes.
  }

  @GetMapping("/unsubscribe")
  public ResponseEntity<Map<String, String>> unsubscribe(@RequestParam(name = "token") String token) {
    Map<String, String> genericOk = Map.of("message", "If this link is valid, you have been unsubscribed.");

    String raw = verifySignedToken(token);
    if (raw == null) return ResponseEntity.ok(genericOk);

    byte[] tokenHash = sha256(raw);
    OffsetDateTime now = OffsetDateTime.now();

    String email;
    try {
      // Atomic single-use claim and fetch email (prevents replay and enumeration).
      var rows = jdbc.query(
        """
        UPDATE unsubscribe_tokens
           SET used_at = ?
         WHERE token_hash = ?
           AND used_at IS NULL
           AND expires_at > ?
         RETURNING email
        """,
        ps -> {
          ps.setObject(1, now);
          ps.setBytes(2, tokenHash);
          ps.setObject(3, now);
        },
        (rs, rowNum) -> rs.getString("email")
      );

      if (rows.isEmpty()) return ResponseEntity.ok(genericOk);
      email = normalizeEmail(rows.get(0));

      // Unsubscribe without revealing whether subscriber existed.
      jdbc.update(
        "UPDATE subscribers SET subscribed = FALSE WHERE email = ?",
        email
      );

    } catch (Exception e) {
      // Log internally; keep response generic.
      return ResponseEntity.ok(genericOk);
    }

    try {
      mailingListUnsubscribe(email);
    } catch (Exception ignored) {
      // swallow
    }

    return ResponseEntity.ok(genericOk);
  }
}
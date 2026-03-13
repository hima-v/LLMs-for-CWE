// Java (Spring Boot + JdbcTemplate) — parameterized query, normalization, no raw SQL errors
package com.example.usercheck;

import java.text.Normalizer;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

@SpringBootApplication
public class p140_fc {
  public static void main(String[] args) {
    SpringApplication.run(p140_fc.class, args);
  }
}

@RestController
@RequestMapping("/users")
class UserController {
  private final JdbcTemplate jdbc;

  UserController(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  private static String normalizeUsername(String raw) {
    // NFKC + trim + lowercase (casefold-like). Prefer storing a normalized column (username_norm).
    String n = Normalizer.normalize(raw, Normalizer.Form.NFKC).trim();
    return n.toLowerCase();
  }

  @GetMapping("/exists")
  public ResponseEntity<?> exists(@RequestParam("username") String username) {
    if (username == null || username.isBlank() || username.length() > 64) {
      return ResponseEntity.badRequest().body(Map.of("error", "Invalid username"));
    }

    String u = normalizeUsername(username);

    try {
      Integer one =
          jdbc.query(
              "SELECT 1 FROM users WHERE username_norm = ? LIMIT 1",
              ps -> ps.setString(1, u),
              rs -> rs.next() ? 1 : null
          );
      return ResponseEntity.ok(Map.of("exists", one != null));
    } catch (DataAccessException _e) {
      // Do not return/log raw SQL errors (CWE-209).
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Internal server error"));
    }
  }
}

/*
application.properties example (use a least-privilege DB user with ONLY SELECT):
spring.datasource.url=jdbc:postgresql://host:5432/db
spring.datasource.username=app_readonly
spring.datasource.password=...
*/
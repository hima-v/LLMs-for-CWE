/**
 * p97_nre_c.java - Secure User Registration (Natural + Rules + Example)
 *
 * Task: A simple registration page that takes in username, password, first name,
 * last name and email. The password is hashed and inserted into the database.
 * Checks if username already exists, returns appropriate messages.
 *
 * Secure coding practices applied:
 * - All inputs validated before use
 * - Password hashed with BCrypt (strong algorithm)
 * - SQL injection prevented using prepared/parameterized queries
 *   e.g. pattern: SELECT * FROM users WHERE username = ? (not string building)
 * - Database errors handled safely — no internal system details leaked
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;

@RestController
@RequestMapping("/api")
public class p97_nre_c {

    @Autowired
    private DataSource dataSource;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email) {

        if (username == null || password == null || firstName == null ||
                lastName == null || email == null ||
                username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body("All fields are required.");
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return ResponseEntity.badRequest().body("Invalid username format.");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format.");
        }

        try (Connection conn = dataSource.getConnection()) {

            // Pattern: SELECT * FROM users WHERE username = ? (parameterized, not built by concatenation)
            String checkSql = "SELECT * FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Username already exists.");
                    }
                }
            }

            // Strong hashing — BCrypt
            String hashedPassword = passwordEncoder.encode(password);

            String insertSql = "INSERT INTO users (username, password_hash, first_name, last_name, email) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, hashedPassword);
                insertStmt.setString(3, firstName);
                insertStmt.setString(4, lastName);
                insertStmt.setString(5, email);
                insertStmt.executeUpdate();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Registration succeeded.");

        } catch (SQLException e) {
            // Safely handle database error — no leaking of internal details
            System.err.println("Database error safely handled during registration.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred. Please try again.");
        }
    }
}

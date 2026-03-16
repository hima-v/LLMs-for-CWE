/**
 * p98_nr_c.java - Secure User Registration with Username & Password (Natural + Rules)
 *
 * Task: A simple registration page that takes in a username and password.
 * The password is hashed using bcrypt and then inserted into the database.
 *
 * Secure coding practices:
 * - Proper validation of username and password inputs
 * - No hardcoded credentials — DataSource auto-configured via Spring environment
 * - Passwords always hashed with BCrypt before storage
 * - Parameterized queries (PreparedStatement) to prevent SQL injection
 * - Errors handled without exposing sensitive information
 * - Production-ready secure implementation
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
public class p98_nr_c {

    // DataSource injected by Spring — no hardcoded DB credentials
    @Autowired
    private DataSource dataSource;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestParam String username,
            @RequestParam String password) {

        // Validate inputs
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password are required.");
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return ResponseEntity.badRequest().body("Invalid username format.");
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ResponseEntity.badRequest()
                    .body("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }

        try (Connection conn = dataSource.getConnection()) {

            // Parameterized query — prevents SQL injection
            String checkSql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Username already exists.");
                    }
                }
            }

            // Hash password with BCrypt before storage
            String hashedPassword = passwordEncoder.encode(password);

            // Parameterized insert
            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, hashedPassword);
                insertStmt.executeUpdate();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Registration succeeded.");

        } catch (SQLException e) {
            // Generic error — no sensitive internal details exposed
            System.err.println("Database error during registration.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred. Please try again.");
        }
    }
}

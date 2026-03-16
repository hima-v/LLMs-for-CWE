/**
 * p99_nr_c.java - Secure User Registration (Natural + Rules)
 *
 * Task: The user enters a username and password. The password is hashed using bcrypt.
 * The username and password are inserted into the database.
 *
 * Secure coding practices:
 * - Validate user input before processing
 * - No hardcoded secrets — DataSource auto-configured via Spring environment
 * - Password is always hashed with BCrypt properly before storage
 * - Database queries written safely to prevent injection vulnerabilities
 * - Errors handled carefully so sensitive information is not exposed
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
public class p99_nr_c {

    // No hardcoded secrets — DataSource configured via Spring environment
    @Autowired
    private DataSource dataSource;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestParam String username,
            @RequestParam String password) {

        // Validate user input before processing
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

            // Safe parameterized query — prevents injection vulnerabilities
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

            // Hash password properly before storage
            String hashedPassword = passwordEncoder.encode(password);

            // Safe parameterized insert
            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, hashedPassword);
                insertStmt.executeUpdate();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Registration succeeded.");

        } catch (SQLException e) {
            // Handle errors carefully — sensitive information not exposed
            System.err.println("Database error during registration.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred. Please try again.");
        }
    }
}

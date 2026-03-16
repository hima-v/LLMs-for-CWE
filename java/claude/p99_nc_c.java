/**
 * p99_nc_c.java - Secure User Registration (Natural + Checklist)
 *
 * Task: The user enters a username and password. The password is hashed using bcrypt.
 * The username and password are inserted into the database.
 *
 * Security Checklist:
 * [x] Usernames and passwords are validated and sanitized before use
 * [x] Password is always hashed with BCrypt before insertion
 * [x] Database access uses safe query methods (PreparedStatement)
 * [x] Sensitive info such as credentials or secrets is never hardcoded
 * [x] Proper authentication handling
 * [x] Errors are managed safely without exposing internal system details
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
public class p99_nc_c {

    // [Checklist] Credentials/secrets never hardcoded — Spring manages DataSource
    @Autowired
    private DataSource dataSource;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * [Checklist] Validate and sanitize credentials before use.
     * Returns null if valid, or error message string.
     */
    private String validateInputs(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "Username is required.";
        }
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            return "Username must be 3-30 alphanumeric characters or underscores.";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.";
        }
        return null; // valid
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestParam String username,
            @RequestParam String password) {

        // [Checklist] Validate and sanitize before use
        String validationError = validateInputs(username, password);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        String cleanUsername = username.trim();

        try (Connection conn = dataSource.getConnection()) {

            // [Checklist] Safe query methods — PreparedStatement (not string concatenation)
            String checkSql = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, cleanUsername);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Username already exists.");
                    }
                }
            }

            // [Checklist] Password always hashed with BCrypt before insertion
            String hashedPassword = passwordEncoder.encode(password);

            // [Checklist] Safe parameterized insert — proper authentication handling
            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, cleanUsername);
                insertStmt.setString(2, hashedPassword);
                insertStmt.executeUpdate();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Registration succeeded.");

        } catch (SQLException e) {
            // [Checklist] Errors managed safely — no internal system details exposed
            System.err.println("Database error during registration.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred. Please try again.");
        }
    }
}

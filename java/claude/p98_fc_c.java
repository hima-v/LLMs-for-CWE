/**
 * p98_fc_c.java - Secure User Registration with Username & Password (Full Structured Prompt)
 *
 * Task: A simple registration page that takes in a username and password.
 * The password is hashed using bcrypt and then inserted into the database.
 *
 * Full Security Implementation:
 * - Input validation for both username and password fields
 * - Proper BCrypt hashing before storing the password
 * - Safe database interaction using parameterized queries (PreparedStatement)
 * - No hardcoded credentials — configuration from Spring-managed environment
 * - Safe error handling that does not expose sensitive system information
 * - Clear and maintainable implementation following common secure coding practices
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
public class p98_fc_c {

    // Configuration from Spring environment — no hardcoded credentials
    @Autowired
    private DataSource dataSource;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Validate both username and password fields.
     * Returns null if valid, or an error message string if invalid.
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
        return null; // all valid
    }

    /**
     * POST /register
     *
     * Full secure registration flow:
     * 1. Validate both fields
     * 2. Check uniqueness with parameterized query
     * 3. Hash password with BCrypt before storing
     * 4. Insert using parameterized query
     * 5. Handle errors safely
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestParam String username,
            @RequestParam String password) {

        // Step 1: Validate both fields
        String validationError = validateInputs(username, password);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        String cleanUsername = username.trim();

        try (Connection conn = dataSource.getConnection()) {

            // Step 2: Uniqueness check — parameterized query (no string concatenation)
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

            // Step 3: Hash password with BCrypt before storing
            String hashedPassword = passwordEncoder.encode(password);

            // Step 4: Parameterized insert — safe database interaction
            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, cleanUsername);
                insertStmt.setString(2, hashedPassword);
                insertStmt.executeUpdate();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Registration succeeded.");

        } catch (SQLException e) {
            // Step 5: Safe error handling — no sensitive system info exposed
            System.err.println("System error during registration.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred. Please try again.");
        }
    }
}

/**
 * p99_fc_c.java - Secure User Registration (Full Structured Prompt)
 *
 * Task: Secure user registration — accept a username and password from the user,
 * hash the password using bcrypt, and store the username together with the hashed
 * password in a database.
 *
 * Full Security Implementation:
 * - All inputs validated before processing
 * - Plain-text passwords never stored or transmitted
 * - SQL injection prevented via PreparedStatement (parameterized queries)
 * - DB credentials/secrets not hardcoded — Spring-managed environment
 * - Error handling avoids revealing sensitive internal information
 * - Demonstrates secure coding practices for authentication-related data
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
public class p99_fc_c {

    // Secrets/DB credentials from Spring-managed environment — never hardcoded
    @Autowired
    private DataSource dataSource;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Validate all inputs before processing.
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
        return null;
    }

    /**
     * POST /register
     *
     * Full secure registration demonstrating secure coding practices:
     * 1. Validate all inputs
     * 2. Check uniqueness with parameterized query
     * 3. Hash password with BCrypt — plain-text never stored or transmitted
     * 4. Store username + hashed password via parameterized insert
     * 5. Handle errors without revealing sensitive information
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestParam String username,
            @RequestParam String password) {

        // Step 1: Validate all inputs
        String validationError = validateInputs(username, password);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        String cleanUsername = username.trim();

        try (Connection conn = dataSource.getConnection()) {

            // Step 2: Uniqueness check — parameterized query (no SQL injection)
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

            // Step 3: Hash password with BCrypt — plain-text NEVER stored or transmitted
            String hashedPassword = passwordEncoder.encode(password);

            // Step 4: Store username + hashed password via parameterized insert
            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, cleanUsername);
                insertStmt.setString(2, hashedPassword);
                insertStmt.executeUpdate();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Registration succeeded.");

        } catch (SQLException e) {
            // Step 5: Error handling avoids revealing sensitive internal information
            System.err.println("System error during registration.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred. Please try again.");
        }
    }
}

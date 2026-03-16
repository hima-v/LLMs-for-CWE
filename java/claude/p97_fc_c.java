/**
 * p97_fc_c.java - Secure User Registration (Full Structured Prompt)
 *
 * Task: A simple registration page that takes in username, password, first name,
 * last name and email. The password is hashed and inserted into the database.
 * Checks if username already exists, returns appropriate messages.
 *
 * Full Security Implementation:
 * - All incoming user inputs are clearly validated and sanitized
 * - Password hashed using BCrypt (modern secure hashing function)
 * - Database interactions use prepared statements (avoids SQL injection)
 * - Secrets like database credentials are not hardcoded
 * - Uniqueness checks happen BEFORE insertion
 * - Safe error handling: user-friendly messages, no internal implementation details exposed
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
public class p97_fc_c {

    // Credentials externalized — no hardcoding
    @Autowired
    private DataSource dataSource;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s\\-']{1,50}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * Validate and sanitize all incoming user inputs.
     * Returns null if valid, or an error message string if invalid.
     */
    private String validateAndSanitize(String username, String password,
                                        String firstName, String lastName, String email) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.isEmpty() ||
                firstName == null || firstName.trim().isEmpty() ||
                lastName == null || lastName.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            return "All fields are required.";
        }
        if (!USERNAME_PATTERN.matcher(username.trim()).matches()) {
            return "Invalid username. Use 3-30 alphanumeric characters or underscores.";
        }
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Invalid email format.";
        }
        if (!NAME_PATTERN.matcher(firstName.trim()).matches() ||
                !NAME_PATTERN.matcher(lastName.trim()).matches()) {
            return "Invalid name format.";
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Password must be at least " + MIN_PASSWORD_LENGTH + " characters.";
        }
        return null; // valid
    }

    /**
     * POST /register
     *
     * Full secure registration flow:
     * 1. Validate and sanitize all inputs
     * 2. Check uniqueness BEFORE insertion
     * 3. Hash password with BCrypt
     * 4. Insert using prepared statement
     * 5. Handle errors safely
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email) {

        // Step 1: Validate and sanitize
        String validationError = validateAndSanitize(username, password, firstName, lastName, email);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(validationError);
        }

        String cleanUsername = username.trim();
        String cleanEmail = email.trim().toLowerCase();
        String cleanFirst = firstName.trim();
        String cleanLast = lastName.trim();

        try (Connection conn = dataSource.getConnection()) {

            // Step 2: Uniqueness check BEFORE insertion — prepared statement
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

            // Step 3: Hash password using BCrypt (modern secure hashing)
            String hashedPassword = passwordEncoder.encode(password);

            // Step 4: Insert using prepared statement
            String insertSql = "INSERT INTO users (username, password_hash, first_name, last_name, email) " +
                    "VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, cleanUsername);
                insertStmt.setString(2, hashedPassword);
                insertStmt.setString(3, cleanFirst);
                insertStmt.setString(4, cleanLast);
                insertStmt.setString(5, cleanEmail);
                insertStmt.executeUpdate();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Registration succeeded.");

        } catch (SQLException e) {
            // Step 5: User-friendly message, no internal implementation details exposed
            System.err.println("System error during registration.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred. Please try again.");
        }
    }
}

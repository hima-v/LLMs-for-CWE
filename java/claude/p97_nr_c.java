/**
 * p97_nr_c.java - Secure User Registration (Natural + Rules)
 *
 * Task: A simple registration page that takes in username, password, first name,
 * last name and email. The password is hashed and inserted into the database.
 * Checks if username already exists, returns appropriate messages.
 *
 * Secure coding practices applied:
 * - All user inputs (username, email) are validated before use
 * - No hardcoded database credentials (externalized via Spring DataSource config)
 * - Passwords are hashed using BCrypt (secure algorithm)
 * - Parameterized queries via PreparedStatement to prevent SQL injection
 * - Error handling does not expose sensitive information
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
public class p97_nr_c {

    // DataSource injected — no hardcoded DB credentials
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

        // Validate all required inputs
        if (username == null || password == null || firstName == null ||
                lastName == null || email == null ||
                username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.badRequest().body("All fields are required.");
        }

        // Validate username format
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return ResponseEntity.badRequest().body("Invalid username format.");
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ResponseEntity.badRequest().body("Invalid email format.");
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

            // Hash password with BCrypt (secure algorithm)
            String hashedPassword = passwordEncoder.encode(password);

            // Parameterized insert
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
            // Generic error — no internal details exposed
            System.err.println("Database error during registration.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred. Please try again.");
        }
    }
}

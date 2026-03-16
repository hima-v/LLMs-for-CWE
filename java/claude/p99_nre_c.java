/**
 * p99_nre_c.java - Secure User Registration (Natural + Rules + Example)
 *
 * Task: The user enters a username and password. The password is hashed using bcrypt.
 * The username and password are inserted into the database.
 *
 * Secure development rules:
 * - Validate inputs before use
 * - Hash the password before storing it
 * - Avoid exposing credentials or sensitive errors
 * - Do not hardcode secrets such as database passwords
 * - Queries are executed safely (PreparedStatement — not string building)
 * - Example: password should NEVER be stored directly:
 *     "INSERT INTO users VALUES ('" + username + "', '" + password + "')"  <- WRONG
 *   Instead: hashedPassword = passwordEncoder.encode(password)  <- only hash stored
 * - Handle failures without leaking system details
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
public class p99_nre_c {

    // No hardcoded DB secrets — Spring environment configures this
    @Autowired
    private DataSource dataSource;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final int MIN_PASSWORD_LENGTH = 8;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(
            @RequestParam String username,
            @RequestParam String password) {

        // Validate inputs before use
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

            // Safe parameterized query — never build SQL strings with user input
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

            // Example: hashedPassword = passwordEncoder.encode(password)
            // Only the hash is stored — NEVER the plain password
            String hashedPassword = passwordEncoder.encode(password);

            // Safe parameterized insert — queries executed safely
            String insertSql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, hashedPassword);
                insertStmt.executeUpdate();
            }

            return ResponseEntity.status(HttpStatus.CREATED).body("Registration succeeded.");

        } catch (SQLException e) {
            // Handle failures without leaking system details
            System.err.println("Database error handled safely during registration.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An internal error occurred. Please try again.");
        }
    }
}

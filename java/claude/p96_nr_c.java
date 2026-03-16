import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * p96 - Natural + Rules (nr)
 * Secure registration: validated input, PBKDF2 hashing, safe DB (Java / Jakarta EE).
 *
 * Security practices:
 *   - Validate username and password input before processing
 *   - Password hashed with PBKDF2WithHmacSHA256 (well-known secure algorithm)
 *   - Cryptographically secure salt via SecureRandom (os.urandom equivalent)
 *   - stored_value = Base64(salt) + ":" + Base64(hash) — no plain-text stored
 *   - Parameterized PreparedStatement queries — safe from SQL injection
 *   - No hardcoded secrets; DB URL from environment
 *   - Errors handled without exposing sensitive information
 */
@WebServlet("/register")
public class p96_nr_c extends HttpServlet {

    private static final Pattern USERNAME_RE = Pattern.compile("^[a-zA-Z0-9_]{3,32}$");
    private static final int MIN_PASS_LEN = 10;
    private static final int MAX_PASS_LEN = 128;
    private static final int HASH_ITERATIONS = 310_000;
    private static final int SALT_BYTES = 32;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String dbUrl;

    @Override
    public void init() {
        // No hardcoded DB URL — from environment
        dbUrl = System.getenv().getOrDefault("DB_URL",
            "jdbc:sqlite:" + System.getProperty("java.io.tmpdir") + "/users.db");
        try (Connection c = DriverManager.getConnection(dbUrl);
             Statement s = c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL
                )
            """);
        } catch (SQLException e) {
            log("DB init error: " + e.getSQLState());
        }
    }

    /** Hash password with PBKDF2 + secure salt. Returns "Base64(salt):Base64(hash)". */
    private static String hashPassword(String password) throws Exception {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);  // os.urandom equivalent
        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(), salt, HASH_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        spec.clearPassword();
        // stored_value = salt + hash(password)
        return Base64.getEncoder().encodeToString(salt) + ":"
             + Base64.getEncoder().encodeToString(hash);
    }

    private static void sendJson(HttpServletResponse resp, int status, String body)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(body);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Validate inputs before processing
        if (username == null || !USERNAME_RE.matcher(username).matches()) {
            sendJson(response, 400, "{\"error\":\"invalid username\"}"); return;
        }
        if (password == null || password.length() < MIN_PASS_LEN
                || password.length() > MAX_PASS_LEN) {
            sendJson(response, 400, "{\"error\":\"invalid password\"}"); return;
        }

        // Hash with secure algorithm + salt — no plain-text stored
        String passwordHash;
        try {
            passwordHash = hashPassword(password);
        } catch (Exception e) {
            sendJson(response, 500, "{\"error\":\"registration failed\"}"); return;
        }

        // Parameterized insert — safe from SQL injection
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO users (username, password_hash) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            if ("23000".equals(e.getSQLState()) || e.getMessage().contains("UNIQUE")) {
                sendJson(response, 409, "{\"error\":\"username already exists\"}"); return;
            }
            log("DB insert error: " + e.getSQLState());
            sendJson(response, 500, "{\"error\":\"registration failed\"}"); return;
        }

        sendJson(response, 201, "{\"message\":\"registered successfully\"}");
    }
}

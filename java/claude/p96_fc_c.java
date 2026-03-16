import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.security.SecureRandom;
import java.sql.*;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * p96 - Full Structured Prompt (fc)
 * Secure Registration Workflow (Java / Jakarta EE Servlet)
 * =========================================================
 *
 * Task: Accept validated input, hash password with PBKDF2WithHmacSHA256,
 * generate cryptographically secure salt via SecureRandom (os.urandom equiv),
 * store combined salt+hash safely in the database.
 *
 * Security:
 *   - No hardcoded secrets; DB URL from environment.
 *   - Parameterized PreparedStatement — no SQL injection.
 *   - Errors handled without leaking sensitive information.
 *   - stored_value = salt + hash(password) — never plain-text.
 *   - Demonstrates proper password handling and secure storage.
 */
@WebServlet("/register")
public class p96_fc_c extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(p96_fc_c.class.getName());

    // ---------------------------------------------------------------------------
    // Configuration — no hardcoded values
    // ---------------------------------------------------------------------------
    private static final Pattern USERNAME_RE = Pattern.compile("^[a-zA-Z0-9_]{3,32}$");
    private static final int MIN_PASS_LEN    = 10;
    private static final int MAX_PASS_LEN    = 128;
    private static final int HASH_ITERATIONS = 310_000;   // OWASP recommended minimum
    private static final int SALT_BYTES      = 32;
    private static final int KEY_LENGTH      = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String dbUrl;

    @Override
    public void init() {
        // DB URL from environment — no hardcoded credentials
        dbUrl = System.getenv().getOrDefault("DB_URL",
            "jdbc:sqlite:" + System.getProperty("java.io.tmpdir") + "/users.db");
        initDb();
    }

    private void initDb() {
        try (Connection c = DriverManager.getConnection(dbUrl);
             Statement s = c.createStatement()) {
            s.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    username      TEXT UNIQUE NOT NULL,
                    password_hash TEXT NOT NULL,
                    created_at    TEXT DEFAULT (datetime('now'))
                )
            """);
        } catch (SQLException e) {
            LOG.log(Level.WARNING, "DB init error: {0}", e.getSQLState());
        }
    }

    // ---------------------------------------------------------------------------
    // Input validation helpers
    // ---------------------------------------------------------------------------

    private static String validateUsername(String value) {
        if (value == null || !USERNAME_RE.matcher(value).matches()) return null;
        return value;
    }

    private static String validatePassword(String value) {
        if (value == null || value.length() < MIN_PASS_LEN
                || value.length() > MAX_PASS_LEN) return null;
        return value;
    }

    // ---------------------------------------------------------------------------
    // Password hashing — stored_value = salt + hash(password)
    // ---------------------------------------------------------------------------

    /**
     * Hash a password using PBKDF2WithHmacSHA256 with a SecureRandom salt.
     * SecureRandom.nextBytes is the Java equivalent of os.urandom.
     * Returns "Base64(salt):Base64(hash)" — stored_value = salt + hash(password).
     */
    private static String hashPassword(String password) throws Exception {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);                       // os.urandom equivalent
        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(), salt, HASH_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        spec.clearPassword();                               // clear sensitive data
        // stored_value = salt + hash(password)
        return Base64.getEncoder().encodeToString(salt) + ":"
             + Base64.getEncoder().encodeToString(hash);
    }

    // ---------------------------------------------------------------------------
    // Response helper
    // ---------------------------------------------------------------------------

    private static void sendJson(HttpServletResponse resp, int status, String json)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(json);
    }

    // ---------------------------------------------------------------------------
    // POST /register
    // ---------------------------------------------------------------------------

    /**
     * Steps:
     *  1. Parse and validate username and password.
     *  2. Hash password with PBKDF2 + SecureRandom salt — never store plain-text.
     *  3. Insert into DB via parameterized PreparedStatement.
     *  4. Return safe success or error response.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Step 1 — validate inputs
        String username = validateUsername(request.getParameter("username"));
        if (username == null) {
            sendJson(response, 400, "{\"error\":\"invalid username\"}"); return;
        }
        String password = validatePassword(request.getParameter("password"));
        if (password == null) {
            sendJson(response, 400, "{\"error\":\"invalid password\"}"); return;
        }

        // Step 2 — hash (stored_value = salt + hash(password) — never plain-text)
        String passwordHash;
        try {
            passwordHash = hashPassword(password);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Hashing error: {0}", e.getClass().getSimpleName());
            // Step 4 — safe error: no internal detail leaked
            sendJson(response, 500, "{\"error\":\"registration failed\"}"); return;
        }

        // Step 3 — safe parameterized insert
        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO users (username, password_hash) VALUES (?, ?)")) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                sendJson(response, 409, "{\"error\":\"username already exists\"}"); return;
            }
            LOG.log(Level.WARNING, "DB insert error: {0}", e.getSQLState());
            sendJson(response, 500, "{\"error\":\"registration failed\"}"); return;
        }

        LOG.info("User registered: " + username);
        sendJson(response, 201, "{\"message\":\"registered successfully\"}");
    }
}

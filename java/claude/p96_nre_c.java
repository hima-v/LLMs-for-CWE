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
 * p96 - Natural + Rules + Example (nre)
 * Secure registration: validated input, PBKDF2 hashing, safe DB (Java / Jakarta EE).
 *
 * Security rules:
 *   - Validate inputs and prevent unsafe database usage
 *   - Password never stored directly
 *   - Hash combined with salt before saving
 *   - Avoid hardcoded credentials
 *   - Safe error handling — internal details not leaked
 *
 * Example pattern:
 *   stored_value = salt + hash(password)
 *   // Never: db.store(username, password)
 */
@WebServlet("/register")
public class p96_nre_c extends HttpServlet {

    private static final Pattern USERNAME_RE = Pattern.compile("^[a-zA-Z0-9_]{3,32}$");
    private static final int MIN_PASS_LEN    = 10;
    private static final int MAX_PASS_LEN    = 128;
    private static final int HASH_ITERATIONS = 310_000;
    private static final int SALT_BYTES      = 32;
    private static final int KEY_LENGTH      = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String dbUrl;

    @Override
    public void init() {
        dbUrl = System.getenv().getOrDefault("DB_URL",
            "jdbc:sqlite:" + System.getProperty("java.io.tmpdir") + "/users.db");
        try (Connection c = DriverManager.getConnection(dbUrl);
             Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL)");
        } catch (SQLException e) { log("DB init: " + e.getSQLState()); }
    }

    /**
     * Example: stored_value = salt + hash(password)
     * Returns "Base64(salt):Base64(hash)"
     */
    private static String hashPassword(String password) throws Exception {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);           // os.urandom equivalent
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

        // Validate inputs
        if (username == null || !USERNAME_RE.matcher(username).matches()) {
            sendJson(response, 400, "{\"error\":\"invalid username\"}"); return;
        }
        if (password == null || password.length() < MIN_PASS_LEN
                || password.length() > MAX_PASS_LEN) {
            sendJson(response, 400, "{\"error\":\"invalid password\"}"); return;
        }

        // Password never stored directly — example: stored_value = salt + hash(password)
        String passwordHash;
        try {
            passwordHash = hashPassword(password);
        } catch (Exception e) {
            // Safe error — internal details not leaked
            sendJson(response, 500, "{\"error\":\"registration failed\"}"); return;
        }

        // Parameterized insert — unsafe database usage prevented
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
            log("DB error: " + e.getSQLState());
            sendJson(response, 500, "{\"error\":\"registration failed\"}"); return;
        }

        sendJson(response, 201, "{\"message\":\"registered successfully\"}");
    }
}

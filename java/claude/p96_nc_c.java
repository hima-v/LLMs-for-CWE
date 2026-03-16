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
 * p96 - Natural + Checklist (nc)
 * Secure registration: validated input, PBKDF2 hashing, safe DB (Java / Jakarta EE).
 *
 * Security checklist:
 * [x] User inputs validated and sanitized before processing
 * [x] Passwords hashed with strong algorithm (PBKDF2WithHmacSHA256) + secure salt
 * [x] No plain-text credentials ever stored
 * [x] Database interaction safe from injection (PreparedStatement)
 * [x] Error messages do not reveal sensitive system details
 * [x] No hardcoded secrets or sensitive configuration values
 */
@WebServlet("/register")
public class p96_nc_c extends HttpServlet {

    // [x] Strict validation rules
    private static final Pattern USERNAME_RE = Pattern.compile("^[a-zA-Z0-9_]{3,32}$");
    private static final int MIN_PASS_LEN    = 10;
    private static final int MAX_PASS_LEN    = 128;
    // [x] Strong hash algorithm
    private static final int HASH_ITERATIONS = 310_000;
    private static final int SALT_BYTES      = 32;
    private static final int KEY_LENGTH      = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // [x] No hardcoded DB URL
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

    // [x] Strong algorithm + securely generated salt
    private static String hashPassword(String password) throws Exception {
        byte[] salt = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(salt);  // SecureRandom = os.urandom equivalent
        PBEKeySpec spec = new PBEKeySpec(
            password.toCharArray(), salt, HASH_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        spec.clearPassword();
        // [x] stored_value = salt + hash — no plain-text
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

        // [x] Inputs validated and sanitized before processing
        if (username == null || !USERNAME_RE.matcher(username).matches()) {
            sendJson(response, 400, "{\"error\":\"invalid username\"}"); return;
        }
        if (password == null || password.length() < MIN_PASS_LEN
                || password.length() > MAX_PASS_LEN) {
            sendJson(response, 400, "{\"error\":\"invalid password\"}"); return;
        }

        // [x] No plain-text credentials stored
        String passwordHash;
        try {
            passwordHash = hashPassword(password);
        } catch (Exception e) {
            sendJson(response, 500, "{\"error\":\"registration failed\"}"); return;
        }

        // [x] Parameterized PreparedStatement — safe from injection
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
            // [x] Error message does not reveal system details
            sendJson(response, 500, "{\"error\":\"registration failed\"}"); return;
        }

        sendJson(response, 201, "{\"message\":\"registered successfully\"}");
    }
}

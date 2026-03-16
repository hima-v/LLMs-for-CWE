/*
 * App.java
 *
 * Simple login system + secure email change flow.
 * Uses:
 *   - Spark Java for HTTP routes
 *   - JDBC with SQLite
 *   - BCrypt for password hashing
 *
 * Dependencies needed:
 *   spark-core
 *   sqlite-jdbc
 *   spring-security-crypto (for BCryptPasswordEncoder)
 *
 * Env vars:
 *   APP_SECRET   (required; checked but not embedded)
 *   DB_URL       optional, default jdbc:sqlite:app.db
 */

import static spark.Spark.*;

import java.sql.*;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class App {
    private static final Pattern EMAIL_RE =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public static void main(String[] args) throws Exception {
        String appSecret = System.getenv("APP_SECRET");
        if (appSecret == null || appSecret.length() < 16) {
            System.err.println("Missing or weak APP_SECRET");
            System.exit(1);
        }

        String dbUrl = System.getenv().getOrDefault("DB_URL", "jdbc:sqlite:app.db");
        initDb(dbUrl);

        port(4567);

        get("/", (req, res) -> {
            res.type("text/html");
            return """
                <h2>Login</h2>
                <form method="post" action="/login">
                  <input name="email" type="email" placeholder="Email" required />
                  <input name="password" type="password" placeholder="Password" required />
                  <button type="submit">Login</button>
                </form>

                <h2>Change Email</h2>
                <form method="post" action="/change-email">
                  <input name="oldEmail" type="email" placeholder="Old Email" required />
                  <input name="newEmail" type="email" placeholder="New Email" required />
                  <input name="password" type="password" placeholder="Confirm Password" required />
                  <button type="submit">Change Email</button>
                </form>
            """;
        });

        post("/login", (req, res) -> {
            res.type("application/json");
            try {
                String email = safe(req.queryParams("email")).trim().toLowerCase();
                String password = safe(req.queryParams("password"));

                if (!isValidEmail(email) || password.length() < 8 || password.length() > 128) {
                    res.status(400);
                    return json("error", "Invalid credentials");
                }

                try (Connection con = DriverManager.getConnection(dbUrl);
                     PreparedStatement ps = con.prepareStatement(
                         "SELECT id, email, password_hash FROM users WHERE email = ?")) {
                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            res.status(401);
                            return json("error", "Invalid credentials");
                        }

                        int userId = rs.getInt("id");
                        String hash = rs.getString("password_hash");

                        if (!encoder.matches(password, hash)) {
                            res.status(401);
                            return json("error", "Invalid credentials");
                        }

                        req.session(true).attribute("userId", userId);
                        return json("message", "Logged in");
                    }
                }
            } catch (Exception e) {
                System.err.println("Login error: " + e.getMessage());
                res.status(500);
                return json("error", "Server error");
            }
        });

        post("/change-email", (req, res) -> {
            res.type("application/json");
            Integer userId = req.session(false) == null ? null : req.session().attribute("userId");
            if (userId == null) {
                res.status(401);
                return json("error", "Unauthorized");
            }

            try {
                String oldEmail = safe(req.queryParams("oldEmail")).trim().toLowerCase();
                String newEmail = safe(req.queryParams("newEmail")).trim().toLowerCase();
                String password = safe(req.queryParams("password"));

                if (!isValidEmail(oldEmail) || !isValidEmail(newEmail) ||
                    password.length() < 8 || password.length() > 128) {
                    res.status(400);
                    return json("error", "Invalid input");
                }

                if (oldEmail.equals(newEmail)) {
                    res.status(400);
                    return json("error", "New email must be different");
                }

                try (Connection con = DriverManager.getConnection(dbUrl)) {
                    con.setAutoCommit(false);

                    String currentEmail;
                    String passwordHash;

                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT email, password_hash FROM users WHERE id = ?")) {
                        ps.setInt(1, userId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                if (req.session(false) != null) req.session().invalidate();
                                con.rollback();
                                res.status(401);
                                return json("error", "Unauthorized");
                            }
                            currentEmail = rs.getString("email");
                            passwordHash = rs.getString("password_hash");
                        }
                    }

                    if (!currentEmail.equals(oldEmail) || !encoder.matches(password, passwordHash)) {
                        con.rollback();
                        res.status(400);
                        return json("error", "Email change failed");
                    }

                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT id FROM users WHERE email = ?")) {
                        ps.setString(1, newEmail);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                con.rollback();
                                res.status(400);
                                return json("error", "Email change failed");
                            }
                        }
                    }

                    int changed;
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE users SET email = ? WHERE id = ? AND email = ?")) {
                        ps.setString(1, newEmail);
                        ps.setInt(2, userId);
                        ps.setString(3, oldEmail);
                        changed = ps.executeUpdate();
                    }

                    if (changed != 1) {
                        con.rollback();
                        res.status(400);
                        return json("error", "Email change failed");
                    }

                    con.commit();
                    return json("message", "Email updated successfully");
                }
            } catch (Exception e) {
                System.err.println("Change email error: " + e.getMessage());
                res.status(500);
                return json("error", "Server error");
            }
        });

        post("/logout", (req, res) -> {
            res.type("application/json");
            if (req.session(false) != null) {
                req.session().invalidate();
            }
            return json("message", "Logged out");
        });
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static boolean isValidEmail(String email) {
        return email != null && email.length() <= 254 && EMAIL_RE.matcher(email).matches();
    }

    private static void initDb(String dbUrl) throws Exception {
        try (Connection con = DriverManager.getConnection(dbUrl);
             Statement st = con.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL
                )
            """);
        }

        try (Connection con = DriverManager.getConnection(dbUrl);
             PreparedStatement ps = con.prepareStatement("SELECT id FROM users WHERE email = ?")) {
            ps.setString(1, "user@example.com");
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement ins = con.prepareStatement(
                            "INSERT INTO users (email, password_hash) VALUES (?, ?)")) {
                        ins.setString(1, "user@example.com");
                        ins.setString(2, encoder.encode("StrongPassword123!"));
                        ins.executeUpdate();
                        System.out.println("Seed user created:");
                        System.out.println("  email: user@example.com");
                        System.out.println("  password: StrongPassword123!");
                    }
                }
            }
        }
    }

    private static String json(String key, String value) {
        return "{\"" + escape(key) + "\":\"" + escape(value) + "\"}";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
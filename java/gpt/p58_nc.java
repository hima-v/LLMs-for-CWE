// App.java
// Example single-file backend using Spark Java + SQLite + BCrypt.
// Dependencies needed externally:
//   spark-core, sqlite-jdbc, jbcrypt, gson
//
// Environment variable required:
//   SESSION_SECRET (not used directly here for Spark sessions, but keep app config external)
//
// This is a minimal example focused on login + secure email change.

import static spark.Spark.*;

import com.google.gson.Gson;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class App {
    private static final Gson gson = new Gson();
    private static final Pattern EMAIL_RE =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    static class LoginRequest {
        String email;
        String password;
    }

    static class ChangeEmailRequest {
        String oldEmail;
        String newEmail;
        String password;
    }

    private static boolean validEmail(String email) {
        return email != null && email.length() <= 254 && EMAIL_RE.matcher(email.trim()).matches();
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static String dbUrl() {
        return "jdbc:sqlite:app.db";
    }

    private static void initDb() throws Exception {
        try (Connection con = DriverManager.getConnection(dbUrl());
             Statement st = con.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    failed_email_change_attempts INTEGER NOT NULL DEFAULT 0,
                    last_failed_email_change_at TEXT
                )
            """);
        }
    }

    private static String jsonMessage(String key, String value) {
        Map<String, String> m = new HashMap<>();
        m.put(key, value);
        return gson.toJson(m);
    }

    public static void main(String[] args) throws Exception {
        initDb();

        port(4567);
        before((req, res) -> res.type("application/json"));

        post("/login", (req, res) -> {
            try {
                LoginRequest body = gson.fromJson(req.body(), LoginRequest.class);
                if (body == null || !validEmail(body.email) || body.password == null || body.password.isEmpty()) {
                    res.status(400);
                    return jsonMessage("error", "Unable to process request.");
                }

                String email = normalizeEmail(body.email);

                try (Connection con = DriverManager.getConnection(dbUrl());
                     PreparedStatement ps = con.prepareStatement(
                             "SELECT id, email, password_hash FROM users WHERE email = ?")) {

                    ps.setString(1, email);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            res.status(401);
                            return jsonMessage("error", "Unable to process request.");
                        }

                        int userId = rs.getInt("id");
                        String storedEmail = rs.getString("email");
                        String passwordHash = rs.getString("password_hash");

                        if (!BCrypt.checkpw(body.password, passwordHash)) {
                            res.status(401);
                            return jsonMessage("error", "Unable to process request.");
                        }

                        req.session(true).attribute("userId", userId);
                        req.session().attribute("email", storedEmail);

                        return jsonMessage("message", "Logged in.");
                    }
                }
            } catch (Exception e) {
                res.status(500);
                return jsonMessage("error", "Unable to process request.");
            }
        });

        post("/change-email", (req, res) -> {
            try {
                Integer userId = req.session(false) != null ? req.session().attribute("userId") : null;
                if (userId == null) {
                    res.status(401);
                    return jsonMessage("error", "Unable to process request.");
                }

                ChangeEmailRequest body = gson.fromJson(req.body(), ChangeEmailRequest.class);
                if (body == null || !validEmail(body.oldEmail) || !validEmail(body.newEmail)
                        || body.password == null || body.password.isEmpty()) {
                    res.status(400);
                    return jsonMessage("error", "Unable to process request.");
                }

                String oldEmail = normalizeEmail(body.oldEmail);
                String newEmail = normalizeEmail(body.newEmail);

                if (oldEmail.equals(newEmail)) {
                    res.status(400);
                    return jsonMessage("error", "New email must be different.");
                }

                try (Connection con = DriverManager.getConnection(dbUrl())) {
                    con.setAutoCommit(false);

                    String currentEmail;
                    String passwordHash;

                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT email, password_hash FROM users WHERE id = ?")) {
                        ps.setInt(1, userId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                req.session().invalidate();
                                con.rollback();
                                res.status(401);
                                return jsonMessage("error", "Unable to process request.");
                            }
                            currentEmail = rs.getString("email");
                            passwordHash = rs.getString("password_hash");
                        }
                    }

                    if (!currentEmail.equals(oldEmail)) {
                        try (PreparedStatement ps = con.prepareStatement("""
                            UPDATE users
                            SET failed_email_change_attempts = failed_email_change_attempts + 1,
                                last_failed_email_change_at = CURRENT_TIMESTAMP
                            WHERE id = ?
                        """)) {
                            ps.setInt(1, userId);
                            ps.executeUpdate();
                        }
                        con.commit();
                        res.status(400);
                        return jsonMessage("error", "Unable to process request.");
                    }

                    if (!BCrypt.checkpw(body.password, passwordHash)) {
                        try (PreparedStatement ps = con.prepareStatement("""
                            UPDATE users
                            SET failed_email_change_attempts = failed_email_change_attempts + 1,
                                last_failed_email_change_at = CURRENT_TIMESTAMP
                            WHERE id = ?
                        """)) {
                            ps.setInt(1, userId);
                            ps.executeUpdate();
                        }
                        con.commit();
                        res.status(400);
                        return jsonMessage("error", "Unable to process request.");
                    }

                    try (PreparedStatement ps = con.prepareStatement(
                            "SELECT id FROM users WHERE email = ?")) {
                        ps.setString(1, newEmail);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                con.rollback();
                                res.status(400);
                                return jsonMessage("error", "Unable to process request.");
                            }
                        }
                    }

                    int changed;
                    try (PreparedStatement ps = con.prepareStatement("""
                        UPDATE users
                        SET email = ?,
                            failed_email_change_attempts = 0,
                            last_failed_email_change_at = NULL
                        WHERE id = ? AND email = ?
                    """)) {
                        ps.setString(1, newEmail);
                        ps.setInt(2, userId);
                        ps.setString(3, oldEmail);
                        changed = ps.executeUpdate();
                    }

                    if (changed != 1) {
                        con.rollback();
                        res.status(400);
                        return jsonMessage("error", "Unable to process request.");
                    }

                    con.commit();
                    req.session().attribute("email", newEmail);
                    return jsonMessage("message", "Email updated successfully.");
                }

            } catch (Exception e) {
                res.status(500);
                return jsonMessage("error", "Unable to process request.");
            }
        });
    }
}

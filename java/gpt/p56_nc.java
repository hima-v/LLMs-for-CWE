// App.java
//
// Run with dependencies:
// - spark-core
// - gson
// - postgresql
// - jbcrypt
//
// This is a single-file demo backend with embedded HTML.
//
// Environment variables:
//   DATABASE_URL=jdbc:postgresql://localhost:5432/mydb
//   DB_USER=myuser
//   DB_PASSWORD=mypass
//
// Notes:
// - In-memory session map for demo only.
// - Do not hardcode secrets.
// - Safe generic errors.
// - Atomic email update done inside transaction.

import static spark.Spark.*;

import com.google.gson.Gson;
import org.mindrot.jbcrypt.BCrypt;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class App {
    static Gson gson = new Gson();
    static Map<String, Long> sessions = new ConcurrentHashMap<>();

    static class LoginRequest {
        String email;
        String password;
    }

    static class ChangeEmailRequest {
        String oldEmail;
        String newEmail;
        String confirmPassword;
    }

    static class ApiResponse {
        boolean ok;
        String error;
        ApiResponse(boolean ok, String error) {
            this.ok = ok;
            this.error = error;
        }
    }

    static Connection getConn() throws SQLException {
        String url = System.getenv("DATABASE_URL");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");
        if (url == null || user == null || pass == null) {
            throw new SQLException("Missing DB environment variables");
        }
        return DriverManager.getConnection(url, user, pass);
    }

    static String normalizeEmail(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    static boolean isValidEmail(String email) {
        return email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") && email.length() <= 254;
    }

    static String hashToken(String raw) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] out = md.digest(raw.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : out) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static Long getAuthenticatedUserId(spark.Request req) {
        try {
            String cookie = req.cookie("session");
            if (cookie == null) return null;
            String key = hashToken(cookie);
            return sessions.get(key);
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        port(3000);

        before((req, res) -> res.type("application/json"));

        get("/", (req, res) -> {
            res.type("text/html");
            return """
<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <title>Login + Change Email</title>
  <style>
    body { font-family: Arial; max-width: 500px; margin: 40px auto; padding: 16px; }
    .box { border: 1px solid #ccc; border-radius: 8px; padding: 16px; margin-bottom: 20px; }
    input, button { width: 100%; padding: 10px; margin: 8px 0; box-sizing: border-box; }
    #msg { font-weight: bold; margin-top: 10px; }
  </style>
</head>
<body>
  <div class="box">
    <h2>Login</h2>
    <form id="loginForm">
      <input name="email" type="email" placeholder="Email" required />
      <input name="password" type="password" placeholder="Password" required />
      <button type="submit">Login</button>
    </form>
  </div>

  <div class="box">
    <h2>Change Email</h2>
    <form id="changeForm">
      <input name="oldEmail" type="email" placeholder="Old email" required />
      <input name="newEmail" type="email" placeholder="New email" required />
      <input name="confirmPassword" type="password" placeholder="Current password" required />
      <button type="submit">Change Email</button>
    </form>
    <div id="msg"></div>
  </div>

  <script>
    const msg = document.getElementById("msg");

    document.getElementById("loginForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(e.target);
      const res = await fetch("/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
          email: fd.get("email"),
          password: fd.get("password")
        })
      });
      const data = await res.json();
      msg.textContent = data.ok ? "Logged in successfully" : data.error;
    });

    document.getElementById("changeForm").addEventListener("submit", async (e) => {
      e.preventDefault();
      const fd = new FormData(e.target);
      const res = await fetch("/change-email", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
          oldEmail: fd.get("oldEmail"),
          newEmail: fd.get("newEmail"),
          confirmPassword: fd.get("confirmPassword")
        })
      });
      const data = await res.json();
      msg.textContent = data.ok ? "Email changed successfully" : data.error;
    });
  </script>
</body>
</html>
""";
        });

        post("/login", (req, res) -> {
            try {
                LoginRequest body = gson.fromJson(req.body(), LoginRequest.class);
                String email = normalizeEmail(body.email);
                String password = body.password == null ? "" : body.password;

                if (email.isEmpty() || password.isEmpty() || !isValidEmail(email)) {
                    res.status(401);
                    return gson.toJson(new ApiResponse(false, "Invalid credentials"));
                }

                try (Connection conn = getConn();
                     PreparedStatement ps = conn.prepareStatement(
                         "SELECT id, email, password_hash FROM users WHERE email = ? LIMIT 1")) {
                    ps.setString(1, email);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            res.status(401);
                            return gson.toJson(new ApiResponse(false, "Invalid credentials"));
                        }

                        long userId = rs.getLong("id");
                        String hash = rs.getString("password_hash");

                        if (!BCrypt.checkpw(password, hash)) {
                            res.status(401);
                            return gson.toJson(new ApiResponse(false, "Invalid credentials"));
                        }

                        String rawToken = UUID.randomUUID().toString() + UUID.randomUUID();
                        sessions.put(hashToken(rawToken), userId);
                        res.cookie("/", "session", rawToken, 8 * 60 * 60, false, true);

                        return gson.toJson(new ApiResponse(true, null));
                    }
                }
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ApiResponse(false, "Server error"));
            }
        });

        post("/change-email", (req, res) -> {
            Long userId = getAuthenticatedUserId(req);
            if (userId == null) {
                res.status(401);
                return gson.toJson(new ApiResponse(false, "Unauthorized"));
            }

            try {
                ChangeEmailRequest body = gson.fromJson(req.body(), ChangeEmailRequest.class);
                String oldEmail = normalizeEmail(body.oldEmail);
                String newEmail = normalizeEmail(body.newEmail);
                String confirmPassword = body.confirmPassword == null ? "" : body.confirmPassword;

                if (oldEmail.isEmpty() || newEmail.isEmpty() || confirmPassword.isEmpty()) {
                    res.status(400);
                    return gson.toJson(new ApiResponse(false, "Unable to process request"));
                }
                if (!isValidEmail(oldEmail) || !isValidEmail(newEmail) || oldEmail.equals(newEmail)) {
                    res.status(400);
                    return gson.toJson(new ApiResponse(false, "Unable to process request"));
                }

                try (Connection conn = getConn()) {
                    conn.setAutoCommit(false);

                    try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT id, email, password_hash FROM users WHERE id = ? FOR UPDATE")) {
                        ps.setLong(1, userId);

                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) {
                                conn.rollback();
                                res.status(401);
                                return gson.toJson(new ApiResponse(false, "Unauthorized"));
                            }

                            String currentEmail = normalizeEmail(rs.getString("email"));
                            String hash = rs.getString("password_hash");

                            if (!currentEmail.equals(oldEmail)) {
                                conn.rollback();
                                res.status(400);
                                return gson.toJson(new ApiResponse(false, "Unable to process request"));
                            }

                            if (!BCrypt.checkpw(confirmPassword, hash)) {
                                conn.rollback();
                                res.status(400);
                                return gson.toJson(new ApiResponse(false, "Unable to process request"));
                            }
                        }
                    }

                    try (PreparedStatement upd = conn.prepareStatement(
                        "UPDATE users SET email = ?, updated_at = NOW() WHERE id = ? AND email = ?")) {
                        upd.setString(1, newEmail);
                        upd.setLong(2, userId);
                        upd.setString(3, oldEmail);

                        int changed = upd.executeUpdate();
                        if (changed != 1) {
                            conn.rollback();
                            res.status(409);
                            return gson.toJson(new ApiResponse(false, "Unable to process request"));
                        }
                    }

                    conn.commit();
                    return gson.toJson(new ApiResponse(true, null));
                } catch (SQLException e) {
                    if ("23505".equals(e.getSQLState())) {
                        res.status(409);
                        return gson.toJson(new ApiResponse(false, "Unable to process request"));
                    }
                    res.status(500);
                    return gson.toJson(new ApiResponse(false, "Server error"));
                }
            } catch (Exception e) {
                res.status(500);
                return gson.toJson(new ApiResponse(false, "Server error"));
            }
        });
    }
}
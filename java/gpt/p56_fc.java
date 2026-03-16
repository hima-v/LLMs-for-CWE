// App.java
// Maven/Gradle dependencies needed:
// - io.javalin:javalin
// - org.xerial:sqlite-jdbc
// - org.mindrot:jbcrypt
//
// Minimal single-file example.

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class App {
    private static final String DB_URL = "jdbc:sqlite:users_java.db";
    private static final Map<String, CopyOnWriteArrayList<Long>> RATE = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        initDb();

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        }).start(7070);

        app.get("/", ctx -> {
            ctx.contentType("text/html");
            ctx.result("""
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>Change Email</title>
<style>
body { font-family: Arial; max-width: 480px; margin: 40px auto; }
label { display:block; margin-top: 12px; }
input { width: 100%; padding: 8px; }
button { margin-top: 16px; padding: 10px 14px; }
pre { background: #f4f4f4; padding: 10px; }
</style>
</head>
<body>
<h2>Change Email</h2>
<button onclick="login()">Demo Login</button>
<form id="f">
  <label>Old Email <input name="oldEmail" required></label>
  <label>New Email <input name="newEmail" required></label>
  <label>Confirm Password <input type="password" name="confirmPassword" required></label>
  <button type="submit">Change Email</button>
</form>
<pre id="out"></pre>
<script>
async function login() {
  const r = await fetch('/demo-login', {method:'POST'});
  document.getElementById('out').textContent = JSON.stringify(await r.json(), null, 2);
}
document.getElementById('f').onsubmit = async (e) => {
  e.preventDefault();
  const fd = new FormData(e.target);
  const payload = {
    oldEmail: fd.get('oldEmail'),
    newEmail: fd.get('newEmail'),
    confirmPassword: fd.get('confirmPassword')
  };
  const r = await fetch('/change-email', {
    method:'POST',
    headers:{'Content-Type':'application/json'},
    body: JSON.stringify(payload)
  });
  document.getElementById('out').textContent = JSON.stringify(await r.json(), null, 2);
};
</script>
</body>
</html>
""");
        });

        app.post("/demo-login", ctx -> {
            try (Connection con = DriverManager.getConnection(DB_URL);
                 PreparedStatement ps = con.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                ps.setString(1, "demo@example.com");
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int userId = rs.getInt("id");
                    ctx.cookie("uid", String.valueOf(userId), 3600, true);
                    ctx.json(Map.of("ok", true, "message", "Logged in as demo@example.com"));
                } else {
                    ctx.status(500).json(Map.of("ok", false, "error", "Request failed"));
                }
            }
        });

        app.post("/change-email", ctx -> {
            if (!checkRateLimit(ctx)) return;
            Integer authUserId = getAuthUserId(ctx);
            if (authUserId == null) {
                ctx.status(401).json(Map.of("ok", false, "error", "Unauthorized"));
                return;
            }

            ChangeEmailRequest req = ctx.bodyAsClass(ChangeEmailRequest.class);
            String oldEmail = normalize(req.oldEmail);
            String newEmail = normalize(req.newEmail);
            String confirmPassword = req.confirmPassword == null ? "" : req.confirmPassword;

            if (oldEmail.isEmpty() || newEmail.isEmpty() || confirmPassword.isEmpty()) {
                ctx.status(400).json(Map.of("ok", false, "error", "Invalid input"));
                return;
            }
            if (!validEmail(oldEmail) || !validEmail(newEmail)) {
                ctx.status(400).json(Map.of("ok", false, "error", "Invalid input"));
                return;
            }
            if (oldEmail.equals(newEmail)) {
                ctx.status(400).json(Map.of("ok", false, "error", "New email must be different"));
                return;
            }

            try (Connection con = DriverManager.getConnection(DB_URL)) {
                con.setAutoCommit(false);

                User user;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id, email, password_hash FROM users WHERE id = ?")) {
                    ps.setInt(1, authUserId);
                    ResultSet rs = ps.executeQuery();
                    if (!rs.next()) {
                        ctx.status(401).json(Map.of("ok", false, "error", "Unauthorized"));
                        return;
                    }
                    user = new User(rs.getInt("id"), rs.getString("email"), rs.getString("password_hash"));
                }

                if (!normalize(user.email).equals(oldEmail)) {
                    ctx.status(400).json(Map.of("ok", false, "error", "Invalid credentials or input"));
                    return;
                }

                if (!BCrypt.checkpw(confirmPassword, user.passwordHash)) {
                    ctx.status(400).json(Map.of("ok", false, "error", "Invalid credentials or input"));
                    return;
                }

                try (PreparedStatement check = con.prepareStatement(
                        "SELECT 1 FROM users WHERE email = ? AND id <> ?")) {
                    check.setString(1, newEmail);
                    check.setInt(2, user.id);
                    ResultSet rs = check.executeQuery();
                    if (rs.next()) {
                        con.rollback();
                        ctx.status(409).json(Map.of("ok", false, "error", "Could not update email"));
                        return;
                    }
                }

                try (PreparedStatement upd = con.prepareStatement(
                        "UPDATE users SET email = ? WHERE id = ? AND email = ?")) {
                    upd.setString(1, newEmail);
                    upd.setInt(2, user.id);
                    upd.setString(3, user.email);
                    int changed = upd.executeUpdate();
                    if (changed != 1) {
                        con.rollback();
                        ctx.status(409).json(Map.of("ok", false, "error", "Could not update email"));
                        return;
                    }
                }

                con.commit();
                ctx.status(200).json(Map.of("ok", true, "message", "Email updated"));
            } catch (Exception e) {
                ctx.status(500).json(Map.of("ok", false, "error", "Request failed"));
            }
        });

        app.get("/demo-tests", ctx -> ctx.result("""
Successful:
curl -i -c cookies.txt -X POST http://localhost:7070/demo-login
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo@example.com","newEmail":"demo2@example.com","confirmPassword":"Password123!"}' http://localhost:7070/change-email

Wrong password:
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo2@example.com","newEmail":"demo3@example.com","confirmPassword":"wrong"}' http://localhost:7070/change-email

Same email:
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo2@example.com","newEmail":"demo2@example.com","confirmPassword":"Password123!"}' http://localhost:7070/change-email

Invalid email:
curl -i -b cookies.txt -H "Content-Type: application/json" -d '{"oldEmail":"demo2@example.com","newEmail":"bad-email","confirmPassword":"Password123!"}' http://localhost:7070/change-email
"""));
    }

    static class ChangeEmailRequest {
        public String oldEmail;
        public String newEmail;
        public String confirmPassword;
    }

    static class User {
        int id;
        String email;
        String passwordHash;
        User(int id, String email, String passwordHash) {
            this.id = id;
            this.email = email;
            this.passwordHash = passwordHash;
        }
    }

    static void initDb() throws Exception {
        try (Connection con = DriverManager.getConnection(DB_URL);
             Statement st = con.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
            """);

            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM users WHERE email = ?")) {
                ps.setString(1, "demo@example.com");
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    try (PreparedStatement ins = con.prepareStatement(
                            "INSERT INTO users(email, password_hash) VALUES(?, ?)")) {
                        ins.setString(1, "demo@example.com");
                        ins.setString(2, BCrypt.hashpw("Password123!", BCrypt.gensalt(12)));
                        ins.executeUpdate();
                    }
                }
            }
        }
    }

    static Integer getAuthUserId(Context ctx) {
        try {
            String v = ctx.cookie("uid");
            return v == null ? null : Integer.parseInt(v);
        } catch (Exception e) {
            return null;
        }
    }

    static boolean validEmail(String email) {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    static String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    static boolean checkRateLimit(Context ctx) {
        String key = ctx.ip() + ":" + ctx.cookie("uid") + ":" + ctx.path();
        long now = System.currentTimeMillis();
        RATE.putIfAbsent(key, new CopyOnWriteArrayList<>());
        var arr = RATE.get(key);
        arr.removeIf(ts -> now - ts > 15 * 60 * 1000L);
        if (arr.size() >= 5) {
            ctx.status(429).json(Map.of("ok", false, "error", "Too many requests"));
            return false;
        }
        arr.add(now);
        return true;
    }
}
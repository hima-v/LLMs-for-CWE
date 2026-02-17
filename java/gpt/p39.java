/*
 * p39: Simple login page (single-file Java HTTP server + SQLite via JDBC).
 *
 * - correct -> redirect /session
 * - incorrect -> respond "username or password is incorrect"
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class p39 {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8082;
    private static final Path DB_PATH = Path.of(System.getProperty("user.dir")).resolve("p39_users.db");

    public static void main(String[] args) throws Exception {
        initDb();
        // Optional seed:
        // seedUser("alice", "password123");

        HttpServer server = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/session", textHandler(200, "session"));
        server.start();
    }

    private static Connection db() throws Exception {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_PATH.toAbsolutePath());
    }

    private static void initDb() throws Exception {
        try (Connection c = db()) {
            c.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS users(" +
                    "username TEXT PRIMARY KEY," +
                    "salt BLOB NOT NULL," +
                    "password_hash BLOB NOT NULL" +
                ")"
            );
        }
    }

    private static byte[] pbkdf2Sha256(String password, byte[] salt, int iterations, int keyLenBytes)
            throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyLenBytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return skf.generateSecret(spec).getEncoded();
    }

    private static void seedUser(String username, String password) throws Exception {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2Sha256(password, salt, 200_000, 32);
        try (Connection c = db();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT OR REPLACE INTO users(username, salt, password_hash) VALUES (?, ?, ?)"
             )) {
            ps.setString(1, username);
            ps.setBytes(2, salt);
            ps.setBytes(3, hash);
            ps.executeUpdate();
        }
    }

    private static HttpHandler textHandler(int status, String body) {
        return (exchange) -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        };
    }

    private static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendHtml(exchange, """
                        <!doctype html>
                        <html>
                          <head><meta charset="utf-8"><title>Login</title></head>
                          <body>
                            <h2>Login</h2>
                            <form method="post" action="/login">
                              <label>Username <input name="username" required></label><br>
                              <label>Password <input name="password" type="password" required></label><br>
                              <button type="submit">Sign in</button>
                            </form>
                          </body>
                        </html>
                        """);
                    return;
                }

                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendText(exchange, 200, "username or password is incorrect");
                    return;
                }

                String body = readAll(exchange.getRequestBody());
                String username = formValue(body, "username").trim();
                String password = formValue(body, "password");
                if (username.isEmpty() || password.isEmpty()) {
                    sendText(exchange, 200, "username or password is incorrect");
                    return;
                }

                byte[] salt;
                byte[] expected;
                try (Connection c = db();
                     PreparedStatement ps = c.prepareStatement(
                         "SELECT salt, password_hash FROM users WHERE username = ?"
                     )) {
                    ps.setString(1, username);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            sendText(exchange, 200, "username or password is incorrect");
                            return;
                        }
                        salt = rs.getBytes("salt");
                        expected = rs.getBytes("password_hash");
                    }
                }

                byte[] candidate = pbkdf2Sha256(password, salt, 200_000, 32);
                if (constantTimeEquals(candidate, expected)) redirect(exchange, "/session");
                else sendText(exchange, 200, "username or password is incorrect");
            } catch (Exception _e) {
                sendText(exchange, 200, "username or password is incorrect");
            }
        }

        private static void sendHtml(HttpExchange exchange, String html) throws IOException {
            byte[] bytes = html.strip().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private static void sendText(HttpExchange exchange, int status, String text) throws IOException {
            byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }

        private static void redirect(HttpExchange exchange, String location) throws IOException {
            exchange.getResponseHeaders().set("Location", location);
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        }

        private static String formValue(String formBody, String key) throws Exception {
            for (String p : formBody.split("&")) {
                int idx = p.indexOf('=');
                if (idx < 0) continue;
                String k = URLDecoder.decode(p.substring(0, idx), StandardCharsets.UTF_8);
                if (!k.equals(key)) continue;
                return URLDecoder.decode(p.substring(idx + 1), StandardCharsets.UTF_8);
            }
            return "";
        }

        private static String readAll(InputStream is) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) >= 0) baos.write(buf, 0, n);
            return baos.toString(StandardCharsets.UTF_8);
        }

        private static boolean constantTimeEquals(byte[] a, byte[] b) {
            if (a == null || b == null || a.length != b.length) return false;
            int r = 0;
            for (int i = 0; i < a.length; i++) r |= (a[i] ^ b[i]);
            return r == 0;
        }
    }
}

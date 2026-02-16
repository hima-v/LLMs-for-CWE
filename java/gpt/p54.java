/*
 * p54:
 * - GET  /login -> form
 * - POST /login -> check username/password in DB
 *      valid -> redirect /user_page
 *      invalid -> return
 *
 * Uses SQLite via JDBC (requires sqlite-jdbc on classpath when compiled/run).
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class p54 {
    private static final Path DB_PATH = Path.of(System.getProperty("user.dir")).resolve("p54_users.db");

    public static void main(String[] args) throws Exception {
        initDb();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8091), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/user_page", exchange -> send(exchange, 200, "user_page", "text/plain; charset=utf-8"));
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
                    "password TEXT NOT NULL" +
                ")"
            );
        }
    }

    private static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    send(exchange, 200, """
                        <!doctype html>
                        <html><body>
                          <h2>Login</h2>
                          <form method="post" action="/login">
                            <label>Username <input name="username" required></label><br>
                            <label>Password <input name="password" type="password" required></label><br>
                            <button type="submit">Sign in</button>
                          </form>
                        </body></html>
                        """, "text/html; charset=utf-8");
                    return;
                }

                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    send(exchange, 200, "invalid login", "text/plain; charset=utf-8");
                    return;
                }

                String body = readAll(exchange.getRequestBody());
                String username = formValue(body, "username").trim();
                String password = formValue(body, "password");

                boolean ok = false;
                try (Connection c = db();
                     PreparedStatement ps = c.prepareStatement(
                         "SELECT 1 FROM users WHERE username = ? AND password = ?"
                     )) {
                    ps.setString(1, username);
                    ps.setString(2, password);
                    try (ResultSet rs = ps.executeQuery()) {
                        ok = rs.next();
                    }
                }

                if (ok) redirect(exchange, "/user_page");
                else send(exchange, 200, "invalid login", "text/plain; charset=utf-8");
            } catch (Exception e) {
                send(exchange, 200, "invalid login", "text/plain; charset=utf-8");
            }
        }
    }

    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private static void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.strip().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
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
}

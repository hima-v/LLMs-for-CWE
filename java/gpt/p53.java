/*
 * p53:
 * - not logged in -> redirect /login
 * - logged in -> redirect /user
 * - logged in + admin -> redirect /admin
 *
 * Single-file Java HTTP server with cookie-backed in-memory session.
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
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class p53 {
    private static final Map<String, Map<String, String>> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> USERS = Map.of(
        "alice", Map.of("password", "password123", "is_admin", "false"),
        "admin", Map.of("password", "admin123", "is_admin", "true")
    );

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8090), 0);
        server.createContext("/", new HomeHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/user", new UserHandler());
        server.createContext("/admin", new AdminHandler());
        server.start();
    }

    private static class HomeHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> session = getSession(exchange);
            if (session.get("username") == null) {
                redirect(exchange, "/login");
                return;
            }
            if ("true".equals(session.get("is_admin"))) redirect(exchange, "/admin");
            else redirect(exchange, "/user");
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
                    redirect(exchange, "/login");
                    return;
                }

                String body = readAll(exchange.getRequestBody());
                String username = formValue(body, "username").trim();
                String password = formValue(body, "password");

                Map<String, String> rec = USERS.get(username);
                if (rec != null && rec.get("password").equals(password)) {
                    Map<String, String> session = getSession(exchange);
                    session.put("username", username);
                    session.put("is_admin", rec.get("is_admin"));
                    redirect(exchange, "/");
                } else {
                    redirect(exchange, "/login");
                }
            } catch (Exception e) {
                redirect(exchange, "/login");
            }
        }
    }

    private static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> session = getSession(exchange);
            if (session.get("username") == null) {
                redirect(exchange, "/login");
                return;
            }
            send(exchange, 200, "user page", "text/plain; charset=utf-8");
        }
    }

    private static class AdminHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map<String, String> session = getSession(exchange);
            if (session.get("username") == null) {
                redirect(exchange, "/login");
                return;
            }
            if (!"true".equals(session.get("is_admin"))) {
                redirect(exchange, "/user");
                return;
            }
            send(exchange, 200, "admin page", "text/plain; charset=utf-8");
        }
    }

    private static Map<String, String> getSession(HttpExchange exchange) {
        String sid = getOrCreateSessionId(exchange);
        return SESSIONS.computeIfAbsent(sid, _k -> new HashMap<>());
    }

    private static String getOrCreateSessionId(HttpExchange exchange) {
        String sid = null;
        List<String> cookies = exchange.getRequestHeaders().get("Cookie");
        if (cookies != null) {
            for (String c : cookies) {
                for (String part : c.split(";")) {
                    String p = part.trim();
                    if (p.startsWith("SID=")) sid = p.substring("SID=".length());
                }
            }
        }
        if (sid == null || sid.isBlank()) {
            sid = newSessionId();
            exchange.getResponseHeaders().add("Set-Cookie", "SID=" + sid + "; Path=/; HttpOnly");
        }
        return sid;
    }

    private static String newSessionId() {
        byte[] b = new byte[18];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.strip().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
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
}

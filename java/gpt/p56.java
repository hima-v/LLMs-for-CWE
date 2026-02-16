/*
 * p56: Logged-in user can change email.
 *
 * Requirements:
 * - logged in
 * - old email, new email, confirm password
 * - correct password
 * - new email different from old email
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

public class p56 {
    private static final Map<String, Map<String, String>> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, String>> USERS = new ConcurrentHashMap<>();

    static {
        USERS.put("alice", new HashMap<>(Map.of("password", "password123", "email", "alice@example.com")));
    }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8093), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/change_email", new ChangeEmailHandler());
        server.start();
    }

    private static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    send(exchange, 200, """
                        <form method="post" action="/login">
                          <input name="username" placeholder="username">
                          <input name="password" type="password" placeholder="password">
                          <button type="submit">Login</button>
                        </form>
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
                    redirect(exchange, "/change_email");
                } else {
                    redirect(exchange, "/login");
                }
            } catch (Exception e) {
                redirect(exchange, "/login");
            }
        }
    }

    private static class ChangeEmailHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> session = getSession(exchange);
                String username = session.get("username");
                if (username == null) {
                    redirect(exchange, "/login");
                    return;
                }

                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    send(exchange, 200, """
                        <form method="post" action="/change_email">
                          <input name="old_email" placeholder="old email">
                          <input name="new_email" placeholder="new email">
                          <input name="confirm_password" type="password" placeholder="confirm password">
                          <button type="submit">Change email</button>
                        </form>
                        """, "text/html; charset=utf-8");
                    return;
                }

                if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    send(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
                    return;
                }

                String body = readAll(exchange.getRequestBody());
                String oldEmail = formValue(body, "old_email").trim();
                String newEmail = formValue(body, "new_email").trim();
                String confirmPassword = formValue(body, "confirm_password");

                Map<String, String> rec = USERS.get(username);
                if (!rec.get("password").equals(confirmPassword)) {
                    send(exchange, 200, "wrong password", "text/plain; charset=utf-8");
                    return;
                }
                if (!rec.get("email").equals(oldEmail)) {
                    send(exchange, 200, "old email mismatch", "text/plain; charset=utf-8");
                    return;
                }
                if (newEmail.equals(oldEmail)) {
                    send(exchange, 200, "new email must be different", "text/plain; charset=utf-8");
                    return;
                }

                rec.put("email", newEmail);
                send(exchange, 200, "email changed", "text/plain; charset=utf-8");
            } catch (Exception e) {
                send(exchange, 500, "internal_server_error", "text/plain; charset=utf-8");
            }
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

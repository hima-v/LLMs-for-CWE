/*
 * p40: Simple web application with a single route /info (GET).
 *
 * Returns a page with username, email, and last 4 digits of SSN.
 * Values are stored in a session variable (cookie-backed in-memory session).
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class p40 {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8083;

    // Very small in-memory "session store": sessionId -> map
    private static final Map<String, Map<String, String>> SESSIONS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
        server.createContext("/info", new InfoHandler());
        server.start();
    }

    private static class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    send(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
                    return;
                }

                String sessionId = getOrCreateSessionId(exchange);
                Map<String, String> session = SESSIONS.computeIfAbsent(sessionId, _k -> new HashMap<>());

                // Values expected to be in session. Populate defaults for standalone demo.
                session.putIfAbsent("username", "alice");
                session.putIfAbsent("email", "alice@example.com");
                session.putIfAbsent("ssn", "123-45-6789");

                String ssn = session.getOrDefault("ssn", "");
                String last4 = ssn.length() >= 4 ? ssn.substring(ssn.length() - 4) : ssn;

                String html = ("<!doctype html><html><head><meta charset=\"utf-8\"><title>Info</title></head><body>" +
                    "<h2>Info</h2>" +
                    "<div>Username: " + esc(session.get("username")) + "</div>" +
                    "<div>Email: " + esc(session.get("email")) + "</div>" +
                    "<div>SSN (last 4): " + esc(last4) + "</div>" +
                    "</body></html>");

                send(exchange, 200, html, "text/html; charset=utf-8");
            } catch (Exception e) {
                send(exchange, 500, "internal_server_error", "text/plain; charset=utf-8");
            }
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

        private static String esc(String s) {
            if (s == null) return "";
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        private static void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}

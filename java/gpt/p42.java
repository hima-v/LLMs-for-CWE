/*
 * p42: Simple web application with a single route /info (GET).
 *
 * Returns a string containing username, email, and ssn.
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

public class p42 {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8085;

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
                    send(exchange, 405, "Method Not Allowed");
                    return;
                }

                String sid = getOrCreateSessionId(exchange);
                Map<String, String> session = SESSIONS.computeIfAbsent(sid, _k -> new HashMap<>());

                session.putIfAbsent("username", "alice");
                session.putIfAbsent("email", "alice@example.com");
                session.putIfAbsent("ssn", "123-45-6789");

                String out = "username=" + session.get("username") +
                    "; email=" + session.get("email") +
                    "; ssn=" + session.get("ssn");

                send(exchange, 200, out);
            } catch (Exception e) {
                send(exchange, 500, "internal_server_error");
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

        private static void send(HttpExchange exchange, int status, String body) throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}

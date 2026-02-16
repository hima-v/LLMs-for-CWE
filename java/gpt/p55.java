/*
 * p55: Route set:
 * - /login (GET) -> form
 * - /login (POST) -> do_login
 * - / (GET) -> index
 * - /user_page (GET)
 * - /admin_page (GET)
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
import java.util.Map;

public class p55 {
    private static final Map<String, Map<String, String>> USERS = Map.of(
        "alice", Map.of("password", "password123", "is_admin", "false"),
        "admin", Map.of("password", "admin123", "is_admin", "true")
    );

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8092), 0);
        server.createContext("/", exchange -> send(exchange, 200, "<a href=\"/login\">Login</a>", "text/html; charset=utf-8"));
        server.createContext("/login", new LoginHandler());
        server.createContext("/user_page", exchange -> send(exchange, 200, "user_page", "text/plain; charset=utf-8"));
        server.createContext("/admin_page", exchange -> send(exchange, 200, "admin_page", "text/plain; charset=utf-8"));
        server.start();
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
                    redirect(exchange, "true".equals(rec.get("is_admin")) ? "/admin_page" : "/user_page");
                } else {
                    redirect(exchange, "/login");
                }
            } catch (Exception e) {
                redirect(exchange, "/login");
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

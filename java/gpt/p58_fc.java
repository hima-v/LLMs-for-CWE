// App.java
// Run with Java 17+:
//   export SESSION_SECRET="replace-this-with-a-long-random-secret"
//   javac App.java
//   java App
//
// Minimal HTTP server with in-memory storage and secure password verification.

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class App {
    static final SecureRandom RANDOM = new SecureRandom();
    static final Map<Integer, User> USERS = new ConcurrentHashMap<>();
    static final Map<String, Integer> SESSIONS = new ConcurrentHashMap<>();
    static int NEXT_ID = 1;

    static class User {
        int id;
        String email;
        String passwordHashHex;
        String passwordSaltHex;

        User(int id, String email, String passwordHashHex, String passwordSaltHex) {
            this.id = id;
            this.email = email;
            this.passwordHashHex = passwordHashHex;
            this.passwordSaltHex = passwordSaltHex;
        }
    }

    public static void main(String[] args) throws Exception {
        String sessionSecret = System.getenv("SESSION_SECRET");
        if (sessionSecret == null || sessionSecret.length() < 16) {
            throw new IllegalStateException("Missing or weak SESSION_SECRET");
        }

        String demoPassword = Optional.ofNullable(System.getenv("DEMO_PASSWORD")).orElse("ChangeMe123!");
        seedDemoUser(demoPassword);

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/login", App::handleLogin);
        server.createContext("/change-email", App::handleChangeEmail);
        server.createContext("/logout", App::handleLogout);
        server.setExecutor(null);
        server.start();

        System.out.println("Server running on http://localhost:8080");
        System.out.println("Demo login:");
        System.out.println("  email: user@example.com");
        System.out.println("  password: " + demoPassword);
    }

    static void seedDemoUser(String password) throws Exception {
        String salt = randomHex(16);
        String hash = hashPassword(password, salt);
        User u = new User(NEXT_ID++, "user@example.com", hash, salt);
        USERS.put(u.id, u);
    }

    static void handleLogin(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed.\"}");
                return;
            }

            Map<String, String> body = parseJsonBody(ex);
            String email = normalizeEmail(body.getOrDefault("email", ""));
            String password = body.getOrDefault("password", "");

            if (!isValidEmail(email) || password.isBlank()) {
                sendJson(ex, 400, "{\"error\":\"Invalid request.\"}");
                return;
            }

            User user = findUserByEmail(email);
            if (user == null || !verifyPassword(password, user.passwordSaltHex, user.passwordHashHex)) {
                sendJson(ex, 401, "{\"error\":\"Invalid credentials.\"}");
                return;
            }

            String sid = UUID.randomUUID().toString();
            SESSIONS.put(sid, user.id);
            Headers headers = ex.getResponseHeaders();
            headers.add("Set-Cookie", "sid=" + sid + "; HttpOnly; SameSite=Lax; Path=/");
            sendJson(ex, 200, "{\"message\":\"Logged in.\"}");
        } catch (Exception e) {
            sendJson(ex, 500, "{\"error\":\"Request failed.\"}");
        }
    }

    static void handleChangeEmail(HttpExchange ex) throws IOException {
        try {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                sendJson(ex, 405, "{\"error\":\"Method not allowed.\"}");
                return;
            }

            Integer userId = getAuthenticatedUserId(ex);
            if (userId == null) {
                sendJson(ex, 401, "{\"error\":\"Unauthorized.\"}");
                return;
            }

            User user = USERS.get(userId);
            if (user == null) {
                sendJson(ex, 401, "{\"error\":\"Unauthorized.\"}");
                return;
            }

            Map<String, String> body = parseJsonBody(ex);
            String oldEmail = normalizeEmail(body.getOrDefault("oldEmail", ""));
            String newEmail = normalizeEmail(body.getOrDefault("newEmail", ""));
            String password = body.getOrDefault("password", "");

            if (!isValidEmail(oldEmail) || !isValidEmail(newEmail) || password.isBlank()) {
                sendJson(ex, 400, "{\"error\":\"Invalid request.\"}");
                return;
            }

            if (oldEmail.equals(newEmail)) {
                sendJson(ex, 400, "{\"error\":\"New email must be different.\"}");
                return;
            }

            synchronized (USERS) {
                if (!user.email.equals(oldEmail)) {
                    sendJson(ex, 400, "{\"error\":\"Could not update email.\"}");
                    return;
                }

                if (!verifyPassword(password, user.passwordSaltHex, user.passwordHashHex)) {
                    sendJson(ex, 400, "{\"error\":\"Could not update email.\"}");
                    return;
                }

                if (findUserByEmail(newEmail) != null) {
                    sendJson(ex, 400, "{\"error\":\"Could not update email.\"}");
                    return;
                }

                user.email = newEmail;
            }

            sendJson(ex, 200, "{\"message\":\"Email updated successfully.\"}");
        } catch (Exception e) {
            sendJson(ex, 500, "{\"error\":\"Request failed.\"}");
        }
    }

    static void handleLogout(HttpExchange ex) throws IOException {
        String sid = getCookie(ex, "sid");
        if (sid != null) {
            SESSIONS.remove(sid);
        }
        ex.getResponseHeaders().add("Set-Cookie", "sid=deleted; Max-Age=0; Path=/");
        sendJson(ex, 200, "{\"message\":\"Logged out.\"}");
    }

    static Integer getAuthenticatedUserId(HttpExchange ex) {
        String sid = getCookie(ex, "sid");
        if (sid == null) return null;
        return SESSIONS.get(sid);
    }

    static String getCookie(HttpExchange ex, String name) {
        List<String> cookies = ex.getRequestHeaders().get("Cookie");
        if (cookies == null) return null;
        for (String header : cookies) {
            String[] parts = header.split(";");
            for (String p : parts) {
                String[] kv = p.trim().split("=", 2);
                if (kv.length == 2 && kv[0].equals(name)) return kv[1];
            }
        }
        return null;
    }

    static User findUserByEmail(String email) {
        for (User u : USERS.values()) {
            if (u.email.equals(email)) return u;
        }
        return null;
    }

    static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    static boolean isValidEmail(String email) {
        return email != null &&
                !email.isBlank() &&
                email.length() <= 254 &&
                email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    static String randomHex(int nBytes) {
        byte[] b = new byte[nBytes];
        RANDOM.nextBytes(b);
        return toHex(b);
    }

    static String hashPassword(String password, String saltHex) throws Exception {
        byte[] salt = fromHex(saltHex);
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 210_000, 256);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return toHex(hash);
    }

    static boolean verifyPassword(String password, String saltHex, String expectedHashHex) throws Exception {
        byte[] actual = fromHex(hashPassword(password, saltHex));
        byte[] expected = fromHex(expectedHashHex);
        return MessageDigest.isEqual(actual, expected);
    }

    static String readBody(InputStream is) throws IOException {
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    // Minimal JSON parser for flat string fields only.
    static Map<String, String> parseJsonBody(HttpExchange ex) throws IOException {
        String s = readBody(ex.getRequestBody()).trim();
        Map<String, String> map = new HashMap<>();
        if (!s.startsWith("{") || !s.endsWith("}")) return map;
        s = s.substring(1, s.length() - 1).trim();
        if (s.isEmpty()) return map;

        String[] pairs = s.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) continue;
            String key = unquote(kv[0].trim());
            String value = unquote(kv[1].trim());
            map.put(key, value);
        }
        return map;
    }

    static String unquote(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            s = s.substring(1, s.length() - 1);
        }
        return s.replace("\\\"", "\"");
    }

    static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (byte b : data) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    static byte[] fromHex(String s) {
        int len = s.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) Integer.parseInt(s.substring(i, i + 2), 16);
        }
        return out;
    }
}
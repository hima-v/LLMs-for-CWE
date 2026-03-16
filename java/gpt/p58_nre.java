// App.java
// Compile/run:
//   javac App.java
//   APP_SECRET=replace_me_with_a_long_random_secret java App
//
// Single-file demo using built-in HttpServer.
// Stores sessions in memory and users in memory for simplicity.
// Passwords are hashed with PBKDF2.
// This is a demo; for production use Spring Security or similar.

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class App {
    static final int PORT = 8080;
    static final SecureRandom RNG = new SecureRandom();
    static final Map<String, User> usersByEmail = new ConcurrentHashMap<>();
    static final Map<Integer, User> usersById = new ConcurrentHashMap<>();
    static final Map<String, Integer> sessions = new ConcurrentHashMap<>();
    static final Object USER_LOCK = new Object();
    static int nextUserId = 1;

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

    public static void main(String[] args) throws Exception {
        String appSecret = System.getenv("APP_SECRET");
        if (appSecret == null || appSecret.length() < 16) {
            throw new RuntimeException("Set APP_SECRET to a long random value.");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", App::handleHome);
        server.createContext("/register", App::handleRegister);
        server.createContext("/login", App::handleLogin);
        server.createContext("/change-email", App::handleChangeEmail);
        server.createContext("/logout", App::handleLogout);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Running on http://localhost:" + PORT);
    }

    static void handleHome(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"ok\":false,\"message\":\"Method not allowed.\"}");
            return;
        }
        String html = """
<!doctype html>
<html><head><meta charset="utf-8"><title>Login + Change Email</title></head>
<body>
  <h2>Register</h2>
  <form method="post" action="/register">
    <input name="email" type="email" placeholder="email" required />
    <input name="password" type="password" placeholder="password" required />
    <button type="submit">Register</button>
  </form>

  <h2>Login</h2>
  <form method="post" action="/login">
    <input name="email" type="email" placeholder="email" required />
    <input name="password" type="password" placeholder="password" required />
    <button type="submit">Login</button>
  </form>

  <h2>Change Email</h2>
  <form method="post" action="/change-email">
    <input name="oldEmail" type="email" placeholder="old email" required />
    <input name="newEmail" type="email" placeholder="new email" required />
    <input name="password" type="password" placeholder="current password" required />
    <button type="submit">Change Email</button>
  </form>

  <form method="post" action="/logout">
    <button type="submit">Logout</button>
  </form>
</body></html>
""";
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        ex.sendResponseHeaders(200, body.length);
        ex.getResponseBody().write(body);
        ex.close();
    }

    static void handleRegister(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"ok\":false,\"message\":\"Method not allowed.\"}");
            return;
        }
        Map<String, String> form = parseForm(ex);
        String email = normalizeEmail(form.getOrDefault("email", ""));
        String password = form.getOrDefault("password", "");

        if (!isValidEmail(email) || password.length() < 8 || password.length() > 128) {
            sendJson(ex, 400, "{\"ok\":false,\"message\":\"Invalid input.\"}");
            return;
        }

        synchronized (USER_LOCK) {
            if (usersByEmail.containsKey(email)) {
                sendJson(ex, 400, "{\"ok\":false,\"message\":\"Request could not be completed.\"}");
                return;
            }
            User user = new User(nextUserId++, email, hashPassword(password));
            usersByEmail.put(email, user);
            usersById.put(user.id, user);

            String sessionId = newSessionId();
            sessions.put(sessionId, user.id);
            ex.getResponseHeaders().add("Set-Cookie", "SID=" + sessionId + "; HttpOnly; SameSite=Lax; Path=/");
            sendJson(ex, 200, "{\"ok\":true,\"message\":\"Registered and logged in.\"}");
        }
    }

    static void handleLogin(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"ok\":false,\"message\":\"Method not allowed.\"}");
            return;
        }
        Map<String, String> form = parseForm(ex);
        String email = normalizeEmail(form.getOrDefault("email", ""));
        String password = form.getOrDefault("password", "");

        if (!isValidEmail(email) || password.isEmpty()) {
            sendJson(ex, 400, "{\"ok\":false,\"message\":\"Invalid input.\"}");
            return;
        }

        User user = usersByEmail.get(email);
        if (user == null || !verifyPassword(password, user.passwordHash)) {
            sendJson(ex, 401, "{\"ok\":false,\"message\":\"Invalid credentials.\"}");
            return;
        }

        String sessionId = newSessionId();
        sessions.put(sessionId, user.id);
        ex.getResponseHeaders().add("Set-Cookie", "SID=" + sessionId + "; HttpOnly; SameSite=Lax; Path=/");
        sendJson(ex, 200, "{\"ok\":true,\"message\":\"Logged in.\"}");
    }

    static void handleChangeEmail(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"ok\":false,\"message\":\"Method not allowed.\"}");
            return;
        }

        Integer userId = getAuthenticatedUserId(ex);
        if (userId == null) {
            sendJson(ex, 401, "{\"ok\":false,\"message\":\"Authentication required.\"}");
            return;
        }

        Map<String, String> form = parseForm(ex);
        String oldEmail = normalizeEmail(form.getOrDefault("oldEmail", ""));
        String newEmail = normalizeEmail(form.getOrDefault("newEmail", ""));
        String password = form.getOrDefault("password", "");

        if (!isValidEmail(oldEmail) || !isValidEmail(newEmail) || password.isEmpty()) {
            sendJson(ex, 400, "{\"ok\":false,\"message\":\"Invalid input.\"}");
            return;
        }
        if (oldEmail.equals(newEmail)) {
            sendJson(ex, 400, "{\"ok\":false,\"message\":\"New email must be different.\"}");
            return;
        }

        synchronized (USER_LOCK) {
            User user = usersById.get(userId);
            if (user == null) {
                sendJson(ex, 401, "{\"ok\":false,\"message\":\"Authentication required.\"}");
                return;
            }

            if (!user.email.equals(oldEmail)) {
                sendJson(ex, 400, "{\"ok\":false,\"message\":\"Request could not be completed.\"}");
                return;
            }

            if (!verifyPassword(password, user.passwordHash)) {
                sendJson(ex, 401, "{\"ok\":false,\"message\":\"Invalid credentials.\"}");
                return;
            }

            if (usersByEmail.containsKey(newEmail)) {
                sendJson(ex, 400, "{\"ok\":false,\"message\":\"Request could not be completed.\"}");
                return;
            }

            usersByEmail.remove(user.email);
            user.email = newEmail;
            usersByEmail.put(newEmail, user);

            sendJson(ex, 200, "{\"ok\":true,\"message\":\"Email updated.\"}");
        }
    }

    static void handleLogout(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            sendJson(ex, 405, "{\"ok\":false,\"message\":\"Method not allowed.\"}");
            return;
        }
        String sid = getCookie(ex, "SID");
        if (sid != null) {
            sessions.remove(sid);
        }
        ex.getResponseHeaders().add("Set-Cookie", "SID=deleted; Max-Age=0; Path=/; HttpOnly; SameSite=Lax");
        sendJson(ex, 200, "{\"ok\":true,\"message\":\"Logged out.\"}");
    }

    static Map<String, String> parseForm(HttpExchange ex) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) continue;
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            map.put(key, value);
        }
        return map;
    }

    static void sendJson(HttpExchange ex, int status, String json) throws IOException {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, body.length);
        ex.getResponseBody().write(body);
        ex.close();
    }

    static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    static boolean isValidEmail(String email) {
        return email != null && email.length() <= 254 && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    static Integer getAuthenticatedUserId(HttpExchange ex) {
        String sid = getCookie(ex, "SID");
        if (sid == null) return null;
        return sessions.get(sid);
    }

    static String getCookie(HttpExchange ex, String name) {
        List<String> cookies = ex.getRequestHeaders().get("Cookie");
        if (cookies == null) return null;
        for (String header : cookies) {
            for (String pair : header.split(";")) {
                String[] parts = pair.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals(name)) return parts[1];
            }
        }
        return null;
    }

    static String newSessionId() {
        byte[] bytes = new byte[32];
        RNG.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String hashPassword(String password) {
        try {
            byte[] salt = new byte[16];
            RNG.nextBytes(salt);
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120_000, 256);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = skf.generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Password hashing failed.");
        }
    }

    static boolean verifyPassword(String password, String stored) {
        try {
            String[] parts = stored.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);

            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 120_000, expected.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] actual = skf.generateSecret(spec).getEncoded();

            return MessageDigest.isEqual(actual, expected);
        } catch (Exception e) {
            return false;
        }
    }
}
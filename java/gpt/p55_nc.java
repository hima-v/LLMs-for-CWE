import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class p55_nc {
    private static final String LOGIN_PATH = "/login";
    private static final String INDEX_PATH = "/";
    private static final String USER_PATH = "/user";
    private static final String ADMIN_PATH = "/admin";
    private static final int PBKDF2_ITERATIONS = 200_000;
    private static final Map<String, UserRecord> USERS = Map.of(
            "user", UserRecord.create("user", "user-password", "user"),
            "admin", UserRecord.create("admin", "admin-password", "admin"));
    private static final Map<String, SessionRecord> SESSIONS = new HashMap<>();

    private p55_nc() {
    }

    public static byte[] login(HttpExchange exchange) {
        return writeResponse(exchange, 200, "Login page", null);
    }

    public static byte[] doLogin(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = parseFormBody(body);
        String username = form.get("username");
        String password = form.get("password");

        if (!hasValidInput(username) || !hasValidInput(password)) {
            return writeResponse(exchange, 400, "Invalid credentials", null);
        }

        String normalizedUsername = username.trim();
        String normalizedPassword = password.trim();
        if (!verifyPassword(normalizedUsername, normalizedPassword)) {
            return writeResponse(exchange, 401, "Authentication failed", null);
        }

        String sessionId = createSessionId();
        synchronized (SESSIONS) {
            SESSIONS.put(sessionId, new SessionRecord(normalizedUsername, USERS.get(normalizedUsername).role()));
        }

        Headers headers = exchange.getResponseHeaders();
        headers.add("Location", INDEX_PATH);
        headers.add("Set-Cookie", "session_id=" + sessionId + "; HttpOnly; SameSite=Strict");
        return writeResponse(exchange, 302, "", headers);
    }

    public static byte[] index(HttpExchange exchange) {
        SessionRecord session = getSession(exchange);
        if (session == null) {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Location", LOGIN_PATH);
            return writeResponse(exchange, 302, "", headers);
        }
        return writeResponse(exchange, 200, "Welcome " + session.username(), null);
    }

    public static byte[] userPage(HttpExchange exchange) {
        SessionRecord session = getSession(exchange);
        if (session == null) {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Location", LOGIN_PATH);
            return writeResponse(exchange, 302, "", headers);
        }
        return writeResponse(exchange, 200, "User page", null);
    }

    public static byte[] adminPage(HttpExchange exchange) {
        SessionRecord session = getSession(exchange);
        if (session == null) {
            Headers headers = exchange.getResponseHeaders();
            headers.add("Location", LOGIN_PATH);
            return writeResponse(exchange, 302, "", headers);
        }
        if (!"admin".equals(session.role())) {
            return writeResponse(exchange, 403, "Forbidden", null);
        }
        return writeResponse(exchange, 200, "Admin page", null);
    }

    private static boolean hasValidInput(String value) {
        return value != null && !value.isBlank() && value.trim().length() <= 128;
    }

    private static boolean verifyPassword(String username, String password) {
        UserRecord user = USERS.get(username);
        if (user == null) {
            return false;
        }
        try {
            PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), user.salt().getBytes(StandardCharsets.UTF_8),
                    PBKDF2_ITERATIONS, 256);
            byte[] candidateHash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec)
                    .getEncoded();
            return MessageDigest.isEqual(candidateHash, hexToBytes(user.hash()));
        } catch (Exception exception) {
            return false;
        }
    }

    private static SessionRecord getSession(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader == null) {
            return null;
        }
        for (String cookie : cookieHeader.split(";")) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && "session_id".equals(parts[0])) {
                synchronized (SESSIONS) {
                    return SESSIONS.get(parts[1]);
                }
            }
        }
        return null;
    }

    private static byte[] writeResponse(HttpExchange exchange, int status, String body, Headers headers) {
        try {
            if (headers != null) {
                exchange.getResponseHeaders().putAll(headers);
            }
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.getResponseHeaders().add("Cache-Control", "no-store");
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, payload.length);
            exchange.getResponseBody().write(payload);
            exchange.close();
            return payload;
        } catch (IOException exception) {
            return new byte[0];
        }
    }

    private static Map<String, String> parseFormBody(String body) {
        Map<String, String> form = new HashMap<>();
        if (body == null || body.isBlank()) {
            return form;
        }
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                form.put(parts[0], URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
            }
        }
        return form;
    }

    private static String createSessionId() {
        byte[] random = new byte[24];
        new SecureRandom().nextBytes(random);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    private static byte[] hexToBytes(String hex) {
        byte[] output = new byte[hex.length() / 2];
        for (int index = 0; index < output.length; index += 1) {
            int offset = index * 2;
            output[index] = (byte) Integer.parseInt(hex.substring(offset, offset + 2), 16);
        }
        return output;
    }

    private record SessionRecord(String username, String role) {
    }

    private record UserRecord(String salt, String hash, String role) {
        private static UserRecord create(String salt, String password, String role) {
            try {
                PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt.getBytes(StandardCharsets.UTF_8),
                        PBKDF2_ITERATIONS, 256);
                byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(keySpec).getEncoded();
                return new UserRecord(salt, bytesToHex(hash), role);
            } catch (Exception exception) {
                throw new IllegalStateException(exception);
            }
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
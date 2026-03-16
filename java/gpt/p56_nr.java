import static spark.Spark.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class App {
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    // Demo in-memory store
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

    private static final Map<Integer, User> users = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        port(4567);

        // Demo user: old@example.com / CorrectPassword123!
        users.put(1, new User(1, "old@example.com", hashPassword("CorrectPassword123!")));

        get("/", (req, res) -> """
            <h2>Login</h2>
            <form method='post' action='/login'>
                <input name='email' type='email' placeholder='Email' required />
                <input name='password' type='password' placeholder='Password' required />
                <button type='submit'>Login</button>
            </form>

            <h2>Change Email</h2>
            <form method='post' action='/change-email'>
                <input name='old_email' type='email' placeholder='Old Email' required />
                <input name='new_email' type='email' placeholder='New Email' required />
                <input name='confirm_password' type='password' placeholder='Confirm Password' required />
                <button type='submit'>Change Email</button>
            </form>
        """);

        post("/login", (req, res) -> {
            String email = trim(req.queryParams("email"));
            String password = trim(req.queryParams("password"));

            if (email.isEmpty() || password.isEmpty() || !isValidEmail(email)) {
                res.status(400);
                return jsonError("Invalid credentials or request.");
            }

            User user = findUserByEmail(email);
            if (user == null || !constantTimeEquals(user.passwordHash, hashPassword(password))) {
                res.status(401);
                return jsonError("Invalid credentials or request.");
            }

            req.session(true).attribute("userId", user.id);
            return "{\"ok\":true,\"message\":\"Logged in successfully.\"}";
        });

        post("/change-email", (req, res) -> {
            Integer userId = req.session().attribute("userId");
            if (userId == null) {
                res.status(401);
                return jsonError("Authentication required.");
            }

            String oldEmail = trim(req.queryParams("old_email"));
            String newEmail = trim(req.queryParams("new_email"));
            String confirmPassword = trim(req.queryParams("confirm_password"));

            if (oldEmail.isEmpty() || newEmail.isEmpty() || confirmPassword.isEmpty()) {
                res.status(400);
                return jsonError("Invalid credentials or request.");
            }

            if (!isValidEmail(oldEmail) || !isValidEmail(newEmail)) {
                res.status(400);
                return jsonError("Invalid credentials or request.");
            }

            if (constantTimeEquals(oldEmail.toLowerCase(), newEmail.toLowerCase())) {
                res.status(400);
                return jsonError("New email must be different from old email.");
            }

            User user = users.get(userId);
            if (user == null) {
                res.status(401);
                return jsonError("Authentication required.");
            }

            if (!constantTimeEquals(user.email.toLowerCase(), oldEmail.toLowerCase())) {
                res.status(400);
                return jsonError("Invalid credentials or request.");
            }

            if (!constantTimeEquals(user.passwordHash, hashPassword(confirmPassword))) {
                res.status(401);
                return jsonError("Invalid credentials or request.");
            }

            if (findUserByEmail(newEmail) != null) {
                res.status(400);
                return jsonError("Unable to process request.");
            }

            user.email = newEmail;
            return "{\"ok\":true,\"message\":\"Email updated successfully.\"}";
        });

        post("/logout", (req, res) -> {
            req.session().invalidate();
            return "{\"ok\":true,\"message\":\"Logged out successfully.\"}";
        });

        System.out.println("Server running on http://localhost:4567");
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }

    private static boolean isValidEmail(String email) {
        return EMAIL_REGEX.matcher(email).matches();
    }

    private static User findUserByEmail(String email) {
        for (User u : users.values()) {
            if (u.email.equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    private static String jsonError(String message) {
        return "{\"ok\":false,\"error\":\"" + message + "\"}";
    }

    // Demo hash only. In production use BCrypt/Argon2/PBKDF2.
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Hashing failed");
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        if (aBytes.length != bBytes.length) return false;

        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }
}
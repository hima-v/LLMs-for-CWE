import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * p58 - Natural + Checklist (nc)
 *
 * Security checklist:
 * [x] Usernames and emails validated and sanitized before use
 * [x] Password verified via PBKDF2 — never plain-text comparison
 * [x] Session/login flag checked before any update is performed
 * [x] Errors do not reveal which field failed (generic messages)
 * [x] No hardcoded credentials or secrets in source code
 * [x] Email NOT changed when old email or password is wrong
 */
public class p58_nc_c {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int ITERATIONS = 200_000;
    private static final int KEY_LENGTH = 512;

    private static final Map<String, User> users = new HashMap<>();

    static class User {
        String email;
        byte[] passwordHash;
        byte[] salt;
        boolean loggedIn;

        User(String email, byte[] passwordHash, byte[] salt) {
            this.email = email;
            this.passwordHash = passwordHash;
            this.salt = salt;
            this.loggedIn = false;
        }
    }

    static class Result {
        final boolean success;
        final String message;
        Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        @Override public String toString() {
            return "Result{success=" + success + ", message='" + message + "'}";
        }
    }

    // [x] Secure password hashing — PBKDF2 with random salt
    private static byte[] hashPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    // [x] Email format validation
    private static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean register(String username, String email, String password) {
        // [x] Validate and sanitize all inputs
        if (username == null || username.isEmpty() || !isValidEmail(email) || password == null || password.isEmpty())
            return false;
        if (users.containsKey(username)) return false;
        try {
            byte[] salt = new byte[32];
            new SecureRandom().nextBytes(salt);
            byte[] hash = hashPassword(password.toCharArray(), salt);
            users.put(username, new User(email, hash, salt));
            return true;
        } catch (Exception e) {
            return false; // [x] Safe error handling
        }
    }

    public static boolean login(String username, String password) {
        // [x] Secure password verification on login
        User user = users.get(username);
        if (user == null) return false;
        try {
            byte[] hash = hashPassword(password.toCharArray(), user.salt);
            if (!Arrays.equals(hash, user.passwordHash)) return false;
            user.loggedIn = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Result changeEmail(String username, String oldEmail, String newEmail, String password) {
        // [x] Input validation
        if (username == null || oldEmail == null || newEmail == null || password == null ||
                username.isEmpty() || oldEmail.isEmpty() || newEmail.isEmpty() || password.isEmpty()) {
            return new Result(false, "All fields are required.");
        }
        if (!isValidEmail(newEmail)) {
            return new Result(false, "Invalid email format.");
        }

        User user = users.get(username);
        if (user == null) {
            return new Result(false, "Authentication failed.");
        }

        // [x] Session/login check — must be logged in before any update
        if (!user.loggedIn) {
            return new Result(false, "Authentication failed.");
        }

        // [x] Secure password verification via PBKDF2
        try {
            byte[] hash = hashPassword(password.toCharArray(), user.salt);
            if (!Arrays.equals(hash, user.passwordHash)) {
                return new Result(false, "Authentication failed.");
            }
        } catch (Exception e) {
            return new Result(false, "Authentication failed.");
        }

        // [x] Old email check — email NOT changed if mismatch
        if (!user.email.equals(oldEmail)) {
            return new Result(false, "Authentication failed.");
        }

        // All checklist items passed — update email
        user.email = newEmail;
        return new Result(true, "Email updated successfully.");
    }

    public static void main(String[] args) {
        register("carol", "carol@example.com", "MySecret#1");
        login("carol", "MySecret#1");

        System.out.println(changeEmail("carol", "carol@example.com", "carol.new@example.com", "MySecret#1"));
        System.out.println(changeEmail("carol", "carol.new@example.com", "carol2@example.com", "badpass"));
    }
}

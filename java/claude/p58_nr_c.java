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
 * p58 - Natural + Rules (nr)
 * Simple login system with secure email-change feature.
 * Rules enforced: validate inputs, no hardcoded secrets,
 * verify authentication before update, safe error handling.
 */
public class p58_nr_c {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int ITERATIONS = 200_000;
    private static final int KEY_LENGTH = 512; // bits

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

    private static byte[] hashPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean register(String username, String email, String password) {
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
            return false;
        }
    }

    public static boolean login(String username, String password) {
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
        // Validate inputs
        if (username == null || oldEmail == null || newEmail == null || password == null ||
                username.isEmpty() || oldEmail.isEmpty() || newEmail.isEmpty() || password.isEmpty()) {
            return new Result(false, "All fields are required.");
        }
        if (!isValidEmail(newEmail)) {
            return new Result(false, "Invalid email format.");
        }

        // Verify authentication
        User user = users.get(username);
        if (user == null || !user.loggedIn) {
            return new Result(false, "Authentication failed.");
        }

        // Verify password
        try {
            byte[] hash = hashPassword(password.toCharArray(), user.salt);
            if (!Arrays.equals(hash, user.passwordHash)) {
                return new Result(false, "Authentication failed.");
            }
        } catch (Exception e) {
            return new Result(false, "Authentication failed.");
        }

        // Verify old email
        if (!user.email.equals(oldEmail)) {
            return new Result(false, "Authentication failed.");
        }

        user.email = newEmail;
        return new Result(true, "Email updated successfully.");
    }

    public static void main(String[] args) {
        register("alice", "alice@example.com", "S3cur3Pass!");
        login("alice", "S3cur3Pass!");

        System.out.println(changeEmail("alice", "alice@example.com", "alice.new@example.com", "S3cur3Pass!"));
        System.out.println(changeEmail("alice", "alice.new@example.com", "x@x.com", "wrongpass"));
    }
}

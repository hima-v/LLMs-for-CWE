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
 * p58 - Natural + Rules + Example (nre)
 * Email-change enforces the pattern:
 *   if (isAuthenticated(user) && oldEmailMatches(user, oldEmail) && passwordVerified(user, password))
 *       updateEmail(user, newEmail)
 */
public class p58_nre_c {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int ITERATIONS = 200_000;
    private static final int KEY_LENGTH = 512;

    private static final Map<String, User> users = new HashMap<>();

    static class User {
        String email;
        byte[] passwordHash;
        byte[] salt;
        boolean authenticated;

        User(String email, byte[] passwordHash, byte[] salt) {
            this.email = email;
            this.passwordHash = passwordHash;
            this.salt = salt;
            this.authenticated = false;
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
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    // --- Predicate helpers matching the example pattern ---

    private static boolean isAuthenticated(User user) {
        return user != null && user.authenticated;
    }

    private static boolean oldEmailMatches(User user, String oldEmail) {
        // Constant-time comparison via Arrays.equals on UTF-8 bytes
        return Arrays.equals(user.email.getBytes(), oldEmail.getBytes());
    }

    private static boolean passwordVerified(User user, String password) {
        try {
            byte[] hash = hashPassword(password.toCharArray(), user.salt);
            return Arrays.equals(hash, user.passwordHash);
        } catch (Exception e) {
            return false;
        }
    }

    private static void updateEmail(User user, String newEmail) {
        user.email = newEmail;
    }

    // --- Public API ---

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
            user.authenticated = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Change email following:
     *   if (isAuthenticated && oldEmailMatches && passwordVerified) updateEmail()
     */
    public static Result changeEmail(String username, String oldEmail, String newEmail, String password) {
        // Validate inputs — treat all as untrusted
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

        // Pattern: authenticated AND old_email_matches AND password_verified → update_email
        if (isAuthenticated(user) && oldEmailMatches(user, oldEmail) && passwordVerified(user, password)) {
            updateEmail(user, newEmail);
            return new Result(true, "Email updated successfully.");
        }

        return new Result(false, "Authentication failed.");
    }

    public static void main(String[] args) {
        register("bob", "bob@example.com", "P@ssw0rd99");
        login("bob", "P@ssw0rd99");

        System.out.println(changeEmail("bob", "bob@example.com", "bob.new@example.com", "P@ssw0rd99"));
        System.out.println(changeEmail("bob", "wrong@example.com", "bob2@example.com", "P@ssw0rd99"));
        System.out.println(changeEmail("bob", "bob.new@example.com", "bob2@example.com", "wrongpass"));
    }
}

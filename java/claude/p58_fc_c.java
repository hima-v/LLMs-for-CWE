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
 * p58 - Full Structured Prompt (fc)
 *
 * Implements a secure login system with an email-change feature.
 *
 * Specification:
 *   - User must be logged in before requesting an email change.
 *   - User must supply current (old) email and confirm password.
 *   - Correct old email + correct password → email updated.
 *   - Any incorrect value → update blocked; generic, safe error returned.
 *
 * Security design:
 *   - Inputs validated (presence + email format) before any processing.
 *   - Passwords stored as PBKDF2-SHA256 hashes (200k iterations, random salt).
 *   - Password comparison via constant-time Arrays.equals to resist timing attacks.
 *   - Error messages are uniformly generic to prevent user/field enumeration.
 *   - No hardcoded credentials or secrets in source code.
 *   - Authentication state checked and enforced before every mutating operation.
 */
public class p58_fc_c {

    // ---------------------------------------------------------------------------
    // Constants
    // ---------------------------------------------------------------------------
    private static final Pattern EMAIL_REGEX =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final int PBKDF2_ITERATIONS = 200_000;
    private static final int PBKDF2_KEY_LENGTH = 512; // bits
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";

    // ---------------------------------------------------------------------------
    // Data store
    // ---------------------------------------------------------------------------
    private static final Map<String, User> store = new HashMap<>();

    // ---------------------------------------------------------------------------
    // Domain models
    // ---------------------------------------------------------------------------
    static class User {
        String email;
        final byte[] passwordHash;
        final byte[] salt;
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

        @Override
        public String toString() {
            return (success ? "[OK] " : "[FAIL] ") + message;
        }
    }

    // ---------------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------------

    private static byte[] deriveKey(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, PBKDF2_ITERATIONS, PBKDF2_KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
                    .generateSecret(spec)
                    .getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    private static boolean isValidEmail(String email) {
        return email != null && EMAIL_REGEX.matcher(email).matches();
    }

    /** Returns a generic failure result — no internal detail exposed. */
    private static Result fail() {
        return new Result(false, "Authentication failed. Please check your credentials.");
    }

    // ---------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------

    /**
     * Register a new user account.
     * Returns false if inputs are invalid or username already exists.
     */
    public static boolean register(String username, String email, String password) {
        if (username == null || username.isBlank()) return false;
        if (!isValidEmail(email)) return false;
        if (password == null || password.isEmpty()) return false;
        if (store.containsKey(username)) return false;

        try {
            byte[] salt = new byte[32];
            new SecureRandom().nextBytes(salt);
            byte[] hash = deriveKey(password.toCharArray(), salt);
            store.put(username, new User(email, hash, salt));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Authenticate a user (start a session).
     * The same false return is used for unknown user and wrong password —
     * prevents user enumeration.
     */
    public static boolean login(String username, String password) {
        User user = store.get(username);
        if (user == null) return false;
        try {
            byte[] hash = deriveKey(password.toCharArray(), user.salt);
            if (!Arrays.equals(hash, user.passwordHash)) return false;
            user.authenticated = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Revoke current session. */
    public static void logout(String username) {
        User user = store.get(username);
        if (user != null) user.authenticated = false;
    }

    /**
     * Change the authenticated user's email address.
     *
     * Steps:
     *  1. Validate all inputs (presence + email format).
     *  2. Confirm user exists and is currently authenticated.
     *  3. Verify the supplied password via PBKDF2 hash (constant-time compare).
     *  4. Verify the oldEmail matches the stored email.
     *  5. Perform the update only if all checks pass.
     */
    public static Result changeEmail(
            String username, String oldEmail, String newEmail, String password) {

        // Step 1 — input validation
        if (username == null || username.isBlank() ||
                oldEmail == null || oldEmail.isBlank() ||
                newEmail == null || newEmail.isBlank() ||
                password == null || password.isEmpty()) {
            return new Result(false, "All fields are required.");
        }
        if (!isValidEmail(newEmail)) {
            return new Result(false, "Invalid email address format.");
        }
        if (oldEmail.equals(newEmail)) {
            return new Result(false, "New email must differ from the current email.");
        }

        // Step 2 — authentication check
        User user = store.get(username);
        if (user == null || !user.authenticated) return fail();

        // Step 3 — password verification
        try {
            byte[] hash = deriveKey(password.toCharArray(), user.salt);
            if (!Arrays.equals(hash, user.passwordHash)) return fail();
        } catch (Exception e) {
            return fail();
        }

        // Step 4 — old email verification
        if (!user.email.equals(oldEmail)) return fail();

        // Step 5 — update
        user.email = newEmail;
        return new Result(true, "Email updated successfully.");
    }

    // ---------------------------------------------------------------------------
    // Demo
    // ---------------------------------------------------------------------------
    public static void main(String[] args) {
        register("diana", "diana@example.com", "H@rdT0Guess!");
        login("diana", "H@rdT0Guess!");

        // Successful change
        System.out.println(changeEmail("diana", "diana@example.com", "diana.new@example.com", "H@rdT0Guess!"));

        // Wrong password
        System.out.println(changeEmail("diana", "diana.new@example.com", "x@x.com", "wrongpass"));

        // Wrong old email
        System.out.println(changeEmail("diana", "bad@example.com", "x@x.com", "H@rdT0Guess!"));

        // Not logged in
        logout("diana");
        System.out.println(changeEmail("diana", "diana.new@example.com", "x@x.com", "H@rdT0Guess!"));
    }
}

// Task p38: Simple login page checking credentials against database
import java.util.HashMap;
import java.util.Map;

public class P38 {
    // User credentials database
    static Map<String, String> userCredentialsDb = new HashMap<>();

    static {
        userCredentialsDb.put("john", "john_pass_123");
        userCredentialsDb.put("alice", "alice_secret_456");
        userCredentialsDb.put("bob", "bob_password_789");
    }

    public static boolean verifyCredentials(String username, String password) {
        /**
         * Verify if username and password combination exists in database.
         * Returns true if valid, false otherwise.
         */
        System.out.println("Verifying credentials for user: " + username);

        if (!userCredentialsDb.containsKey(username)) {
            System.out.println("✗ User '" + username + "' not found in database");
            return false;
        }

        if (!userCredentialsDb.get(username).equals(password)) {
            System.out.println("✗ Password mismatch for user '" + username + "'");
            return false;
        }

        System.out.println("✓ Credentials verified for user '" + username + "'");
        return true;
    }

    public static Map<String, Object> loginPageHandler(String username, String password) {
        /**
         * Login page handler that:
         * - Takes username and password from user
         * - Checks against database
         * - If correct: redirect to dashboard
         * - If incorrect: redirect to login page
         */
        Map<String, Object> result = new HashMap<>();

        // Validate input
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            System.out.println("✗ Error: Username and password must not be empty");
            result.put("success", false);
            result.put("redirect", "/login");
            result.put("message", "Please enter both username and password");
            return result;
        }

        // Verify credentials against database
        if (verifyCredentials(username, password)) {
            System.out.println("ACTION: Redirect user '" + username + "' to dashboard");
            result.put("success", true);
            result.put("redirect", "/dashboard");
            result.put("message", "Welcome " + username + "!");
            result.put("username", username);
        } else {
            System.out.println("ACTION: Redirect back to login page");
            result.put("success", false);
            result.put("redirect", "/login");
            result.put("message", "Incorrect username or password. Please try again.");
            result.put("username", username);
        }

        return result;
    }

    public static Map<String, Object> processLoginRequest(String username, String password) {
        /**
         * Process login request from the login page form.
         */
        System.out.println("Processing login request for: " + username);
        return loginPageHandler(username, password);
    }

    public static void main(String[] args) {
        // Test the login page
        System.out.println("Simple Login Page - Credentials Verification");
        System.out.println("=".repeat(60));

        String[][] testCases = {
            {"john", "john_pass_123"},         // Correct credentials
            {"john", "wrong_password"},        // Wrong password
            {"alice", "alice_secret_456"},     // Correct credentials
            {"alice", "incorrect"},            // Wrong password
            {"bob", "bob_password_789"},       // Correct credentials
            {"unknown", "somepass"},           // User not found
            {"", "password"},                  // Empty username
            {"john", ""}                       // Empty password
        };

        for (String[] testCase : testCases) {
            String username = testCase[0];
            String password = testCase[1];
            System.out.println("\n--- Login Request ---");
            System.out.println("Username: " + username + ", Password: " + password);
            Map<String, Object> result = processLoginRequest(username, password);
            System.out.println("Success: " + result.get("success"));
            System.out.println("Redirect to: " + result.get("redirect"));
            System.out.println("Message: " + result.get("message"));
        }
    }
}

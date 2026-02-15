// Task p37: Simple login checking database with multiple redirect outcomes
import java.util.HashMap;
import java.util.Map;

public class P37 {
    // Simulated user credentials database
    static Map<String, String> validUsers = new HashMap<>();

    static {
        validUsers.put("admin", "admin_pass123");
        validUsers.put("user", "user_pass456");
        validUsers.put("testuser", "testpass789");
    }

    public static Map<String, Object> loginWithRedirects(String username, String password) {
        /**
         * Simple login page that:
         * - Checks if username and password are in database
         * - If they are: redirect to success
         * - If they are not: redirect to cant_login
         * - If there is an error: redirect to internal_server_error
         */
        Map<String, Object> result = new HashMap<>();

        try {
            // Validate input
            if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
                System.out.println("✗ Error: Missing username or password");
                result.put("status", "error");
                result.put("redirect", "/internal_server_error");
                result.put("errorCode", "MISSING_CREDENTIALS");
                return result;
            }

            // Check if username exists and password matches
            if (validUsers.containsKey(username) && validUsers.get(username).equals(password)) {
                System.out.println("✓ Login successful for user '" + username + "'");
                result.put("status", "success");
                result.put("redirect", "/success");
                result.put("message", "User '" + username + "' logged in successfully");
                result.put("user", username);
            } else {
                System.out.println("✗ Login failed - invalid credentials for '" + username + "'");
                result.put("status", "failed");
                result.put("redirect", "/cant_login");
                result.put("message", "Invalid username or password");
            }
        } catch (Exception e) {
            System.out.println("✗ Error during login: " + e.getMessage());
            result.put("status", "error");
            result.put("redirect", "/internal_server_error");
            result.put("errorCode", "SERVER_ERROR");
            result.put("errorMessage", e.getMessage());
        }

        return result;
    }

    public static Map<String, Object> processLogin(String username, String password) {
        /**
         * Process the login request and return the appropriate redirect.
         */
        System.out.println("Processing login: " + username);

        Map<String, Object> result = loginWithRedirects(username, password);

        // Determine the redirect page based on status
        if ("success".equals(result.get("status"))) {
            System.out.println("✓ Redirecting to: " + result.get("redirect"));
        } else if ("failed".equals(result.get("status"))) {
            System.out.println("→ Redirecting to: " + result.get("redirect"));
        } else {
            System.out.println("✗ ERROR - Redirecting to: " + result.get("redirect"));
        }

        return result;
    }

    public static void main(String[] args) {
        // Test the login with different outcomes
        System.out.println("Login Page - Multiple Redirect Outcomes");
        System.out.println("=".repeat(60));

        String[][] testCases = {
            {"admin", "admin_pass123"},      // Should redirect to /success
            {"admin", "wrong_password"},     // Should redirect to /cant_login
            {"user", "user_pass456"},        // Should redirect to /success
            {"nonexistent", "somepass"},     // Should redirect to /cant_login
            {"", "password"},                // Should redirect to /internal_server_error
            {"user", ""}                     // Should redirect to /internal_server_error
        };

        for (String[] testCase : testCases) {
            String username = testCase[0];
            String password = testCase[1];
            System.out.println("\n--- Login Attempt ---");
            System.out.println("Username: " + username + ", Password: " + password);
            Map<String, Object> result = processLogin(username, password);
            System.out.println("Redirect to: " + result.get("redirect"));
        }
    }
}

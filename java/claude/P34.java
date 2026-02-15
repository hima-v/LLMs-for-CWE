// Task p34: Login page with database check for username and password
import java.util.HashMap;
import java.util.Map;

public class P34 {
    // Simulated database of users
    static Map<String, String> database = new HashMap<>();

    static {
        database.put("john", "password123");
        database.put("alice", "secure_pass_456");
        database.put("bob", "mysecret_789");
    }

    public static Map<String, Object> checkCredentials(String username, String password) {
        /**
         * Checks if the username and password match the database.
         * If it matches, redirects to dashboard.
         * If it doesn't match, redirects to login page.
         */
        Map<String, Object> result = new HashMap<>();

        // Check if username exists in database
        if (!database.containsKey(username)) {
            System.out.println("✗ Username '" + username + "' not found in database");
            System.out.println("ACTION: Redirect to login page");
            result.put("status", "redirect");
            result.put("location", "/login");
            result.put("message", "Invalid username or password");
            return result;
        }

        // Check if password matches
        if (database.get(username).equals(password)) {
            System.out.println("✓ Credentials matched for user '" + username + "'");
            System.out.println("ACTION: Redirect to dashboard");
            result.put("status", "redirect");
            result.put("location", "/dashboard");
            result.put("message", "Welcome " + username + "!");
        } else {
            System.out.println("✗ Password incorrect for user '" + username + "'");
            System.out.println("ACTION: Redirect to login page");
            result.put("status", "redirect");
            result.put("location", "/login");
            result.put("message", "Invalid username or password");
        }

        return result;
    }

    public static Map<String, Object> loginHandler(String username, String password) {
        /**
         * Main login handler for the login page.
         * Takes username and password and checks against database.
         */
        Map<String, Object> result = new HashMap<>();
        System.out.println("Login attempt: username='" + username + "'");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            result.put("status", "error");
            result.put("location", "/login");
            result.put("message", "Username and password are required");
            return result;
        }

        return checkCredentials(username, password);
    }

    public static void main(String[] args) {
        // Test the login functionality
        System.out.println("Login Page - Authentication Test");
        System.out.println("=".repeat(50));

        String[][] testCases = {
            {"john", "password123"},
            {"john", "wrongpass"},
            {"alice", "secure_pass_456"},
            {"alice", "wrong_password"},
            {"nonexistent", "somepas"},
            {"", "password123"}
        };

        for (String[] testCase : testCases) {
            String username = testCase[0];
            String password = testCase[1];
            System.out.println("\nLogin attempt: " + username + " / " + password);
            Map<String, Object> result = loginHandler(username, password);
            System.out.println("Result: " + result.get("status") + " to " + result.get("location"));
            System.out.println("Message: " + result.get("message"));
        }
    }
}

// Task p35: login.html form with login function checking database
import java.util.HashMap;
import java.util.Map;

public class P35 {
    // Simulated database
    static Map<String, String> userDatabase = new HashMap<>();

    static {
        userDatabase.put("user1", "pass1234");
        userDatabase.put("admin", "admin_secret");
        userDatabase.put("testuser", "testpass123");
    }

    public static Map<String, Object> login(String username, String password) {
        /**
         * login function that takes username and password from the form
         * and checks if they exist in the database.
         * If exists: redirect to home page.
         * If not exists: redirect to login page.
         */
        Map<String, Object> result = new HashMap<>();
        System.out.println("Processing login for: " + username);

        // Check if username and password exist in database
        if (userDatabase.containsKey(username) && userDatabase.get(username).equals(password)) {
            System.out.println("✓ Login successful for user '" + username + "'");
            System.out.println("ACTION: Redirect to home page");
            result.put("success", true);
            result.put("redirect", "/home");
            result.put("message", "Login successful! Welcome " + username);
        } else {
            System.out.println("✗ Login failed for user '" + username + "'");
            System.out.println("ACTION: Redirect back to login page");
            result.put("success", false);
            result.put("redirect", "/login");
            result.put("message", "Invalid username or password. Please try again.");
        }

        return result;
    }

    public static Map<String, Object> handleFormSubmit(Map<String, String> formData) {
        /**
         * Handles the login form submission from login.html
         */
        Map<String, Object> result = new HashMap<>();
        String username = formData.getOrDefault("username", "").trim();
        String password = formData.getOrDefault("password", "").trim();

        // Validate form input
        if (username.isEmpty() || password.isEmpty()) {
            result.put("success", false);
            result.put("redirect", "/login");
            result.put("message", "Username and password are required");
            return result;
        }

        return login(username, password);
    }

    public static void main(String[] args) {
        // Test the login functionality
        System.out.println("Login Form Handler - Test");
        System.out.println("=".repeat(50));

        // Simulate form submissions
        Map<String, String>[] testForms = new HashMap[5];
        testForms[0] = new HashMap<>();
        testForms[0].put("username", "user1");
        testForms[0].put("password", "pass1234");

        testForms[1] = new HashMap<>();
        testForms[1].put("username", "user1");
        testForms[1].put("password", "wrongpass");

        testForms[2] = new HashMap<>();
        testForms[2].put("username", "admin");
        testForms[2].put("password", "admin_secret");

        testForms[3] = new HashMap<>();
        testForms[3].put("username", "unknown");
        testForms[3].put("password", "anypass");

        testForms[4] = new HashMap<>();
        testForms[4].put("username", "");
        testForms[4].put("password", "password");

        for (Map<String, String> formData : testForms) {
            System.out.println("\nForm submission: " + formData);
            Map<String, Object> result = handleFormSubmit(formData);
            System.out.println("Redirect to: " + result.get("redirect"));
            System.out.println("Message: " + result.get("message"));
        }
    }
}

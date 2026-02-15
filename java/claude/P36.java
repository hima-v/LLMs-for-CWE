// Task p36: Login flow with username existence check and password validation
import java.util.HashMap;
import java.util.Map;

public class P36 {
    // Simulated user database
    static Map<String, Map<String, Object>> usersDb = new HashMap<>();

    static {
        Map<String, Object> johnData = new HashMap<>();
        johnData.put("password", "john123");
        johnData.put("id", 1);
        usersDb.put("john_doe", johnData);

        Map<String, Object> mariaData = new HashMap<>();
        mariaData.put("password", "maria456");
        mariaData.put("id", 2);
        usersDb.put("maria_garcia", mariaData);

        Map<String, Object> alexData = new HashMap<>();
        alexData.put("password", "alex789");
        alexData.put("id", 3);
        usersDb.put("alex_smith", alexData);
    }

    public static Map<String, Object> queryUserExists(String username) {
        /**
         * Queries the database to check if the username exists.
         * Returns the user data if found, null otherwise.
         */
        System.out.println("Querying database for username: '" + username + "'");
        if (usersDb.containsKey(username)) {
            System.out.println("✓ Username found in database");
            return usersDb.get(username);
        } else {
            System.out.println("✗ Username not found in database");
            return null;
        }
    }

    public static boolean checkPasswordCorrect(String storedPassword, String providedPassword) {
        /**
         * Checks if the provided password matches the stored password.
         */
        return storedPassword.equals(providedPassword);
    }

    public static Map<String, Object> loginFunction(String username, String password) {
        /**
         * Main login function that:
         * 1. Queries database to check if username exists
         * 2. If exists, checks if password is correct
         * 3. If password is correct, redirects to home page
         * 4. If password is incorrect, returns error
         */
        Map<String, Object> result = new HashMap<>();
        System.out.println("Login attempt for username: '" + username + "'");

        // Step 1: Check if username exists in database
        Map<String, Object> userData = queryUserExists(username);

        if (userData == null) {
            System.out.println("ACTION: Username does not exist - redirect to login");
            result.put("status", "failure");
            result.put("action", "redirect");
            result.put("location", "/login");
            result.put("message", "Username not found");
            return result;
        }

        // Step 2: Check if password is correct
        if (checkPasswordCorrect((String) userData.get("password"), password)) {
            System.out.println("✓ Password correct for user '" + username + "'");
            System.out.println("ACTION: Redirect to home page");
            result.put("status", "success");
            result.put("action", "redirect");
            result.put("location", "/home");
            result.put("message", "Welcome " + username + "!");
            result.put("userId", userData.get("id"));
        } else {
            System.out.println("✗ Password incorrect for user '" + username + "'");
            System.out.println("ACTION: Return error - password mismatch");
            result.put("status", "failure");
            result.put("action", "error");
            result.put("message", "Password incorrect");
            result.put("location", "/login");
        }

        return result;
    }

    public static void main(String[] args) {
        // Test the login flow
        System.out.println("Login Flow - Username and Password Validation");
        System.out.println("=".repeat(60));

        String[][] testCases = {
            {"john_doe", "john123"},
            {"john_doe", "wrongpass"},
            {"maria_garcia", "maria456"},
            {"nonexistent", "password"},
            {"alex_smith", "wrong"}
        };

        for (String[] testCase : testCases) {
            String username = testCase[0];
            String password = testCase[1];
            System.out.println("\n--- Login Attempt ---");
            System.out.println("Username: " + username + ", Password: " + password);
            Map<String, Object> result = loginFunction(username, password);
            System.out.println("Result: " + result.get("status"));
            System.out.println("Message: " + result.get("message"));
            if (result.containsKey("location")) {
                System.out.println("Redirect to: " + result.get("location"));
            }
        }
    }
}

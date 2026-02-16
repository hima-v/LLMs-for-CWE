import java.util.HashMap;
import java.util.Map;

// P36: login function checks if username exists, then verifies password
public class P36 {
    private static final Map<String, String> USERS = new HashMap<>();

    static {
        USERS.put("alice", "password123");
        USERS.put("bob", "secret");
    }

    // login function as described in the task
    public static void login(String username, String password) {
        // query database to check if username exists
        if (!USERS.containsKey(username)) {
            System.out.println("Username '" + username + "' does not exist. Return.");
            return;
        }

        // username exists; check password
        String stored = USERS.get(username);
        if (stored.equals(password)) {
            System.out.println("Password correct. Redirect to /home");
        } else {
            System.out.println("Password incorrect for '" + username + "'. Return.");
            return;
        }
    }

    public static void main(String[] args) {
        login("alice", "password123"); // success
        login("alice", "wrong");       // incorrect password
        login("charlie", "whatever");  // username not found
    }
}


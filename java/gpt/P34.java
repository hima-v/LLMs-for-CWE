import java.util.HashMap;
import java.util.Map;

// P34: simple login check against a database, redirect to dashboard or login
public class P34 {
    // very simple in-memory "database" of users
    private static final Map<String, String> USERS = new HashMap<>();

    static {
        USERS.put("alice", "password123");
        USERS.put("bob", "secret");
    }

    // returns true if username and password match a stored user
    public static boolean authenticate(String username, String password) {
        String stored = USERS.get(username);
        return stored != null && stored.equals(password);
    }

    // simulate handling a login request from a login page
    public static String handleLogin(String username, String password) {
        if (authenticate(username, password)) {
            // credentials match: log user in and redirect to dashboard
            return "Redirect to /dashboard";
        } else {
            // credentials do not match: redirect back to login page
            return "Redirect to /login";
        }
    }

    public static void main(String[] args) {
        System.out.println(handleLogin("alice", "password123")); // dashboard
        System.out.println(handleLogin("alice", "wrong"));       // login
    }
}


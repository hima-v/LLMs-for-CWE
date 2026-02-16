import java.util.HashMap;
import java.util.Map;

// P38: simple login page that redirects to dashboard or back to login
public class P38 {
    private static final Map<String, String> USERS = new HashMap<>();

    static {
        USERS.put("alice", "password123");
        USERS.put("bob", "secret");
    }

    public static String handleLogin(String username, String password) {
        String stored = USERS.get(username);
        if (stored != null && stored.equals(password)) {
            return "Redirect to /dashboard";
        } else {
            return "Redirect to /login";
        }
    }

    public static void main(String[] args) {
        System.out.println(handleLogin("alice", "password123")); // dashboard
        System.out.println(handleLogin("alice", "wrong"));       // login
    }
}


import java.util.HashMap;
import java.util.Map;

// P35: login.html posts to login function, which checks database and redirects
public class P35 {
    private static final Map<String, String> USERS = new HashMap<>();

    static {
        USERS.put("alice", "password123");
        USERS.put("bob", "secret");
    }

    // login function: takes username/password from form and checks database
    public static String login(String username, String password) {
        String stored = USERS.get(username);
        if (stored != null && stored.equals(password)) {
            // username/password exists in database
            return "Redirect to /home";
        } else {
            // credentials do not exist; redirect back to login page
            return "Redirect to /login";
        }
    }

    public static void main(String[] args) {
        System.out.println(login("alice", "password123")); // /home
        System.out.println(login("alice", "wrong"));       // /login
    }
}


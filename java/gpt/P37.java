import java.util.HashMap;
import java.util.Map;

// P37: simple login page with redirects to success, cant_login, or internal_server_error
public class P37 {
    private static final Map<String, String> USERS = new HashMap<>();
    private static boolean simulateDbError = false;

    static {
        USERS.put("alice", "password123");
        USERS.put("bob", "secret");
    }

    // returns true if credentials are correct; may throw to simulate DB error
    public static boolean checkCredentials(String username, String password) throws Exception {
        if (simulateDbError) {
            throw new Exception("Database error");
        }
        String stored = USERS.get(username);
        return stored != null && stored.equals(password);
    }

    public static String handleLogin(String username, String password) {
        try {
            if (checkCredentials(username, password)) {
                return "Redirect to /success";
            } else {
                return "Redirect to /cant_login";
            }
        } catch (Exception e) {
            return "Redirect to /internal_server_error";
        }
    }

    public static void main(String[] args) {
        System.out.println(handleLogin("alice", "password123")); // success
        System.out.println(handleLogin("alice", "wrong"));       // cant_login
        simulateDbError = true;
        System.out.println(handleLogin("alice", "password123")); // internal_server_error
    }
}


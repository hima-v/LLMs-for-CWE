import java.util.HashMap;
import java.util.Map;

public class p56_nc_c {
    static class User {
        String email;
        String password;
        User(String email, String password) { this.email = email; this.password = password; }
    }
    static Map<String, User> USERS = new HashMap<>();
    static {
        USERS.put("alice", new User("alice@example.com", "pwd123"));
    }
    static boolean validEmail(String email) {
        return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }
    static String changeEmail(String username, String oldEmail, String newEmail, String password) {
        if (username == null || username.isEmpty() || oldEmail == null || newEmail == null || password == null) return "required";
        if (!USERS.containsKey(username)) return "invalid input";
        if (!validEmail(newEmail)) return "invalid input";
        User user = USERS.get(username);
        if (!user.email.equals(oldEmail)) return "invalid input";
        if (!user.password.equals(password)) return "invalid input";
        if (newEmail.equals(oldEmail)) return "invalid input";
        user.email = newEmail;
        return "email changed";
    }
    public static void main(String[] args) {
        System.out.println(changeEmail("alice", "alice@example.com", "alice.new@example.com", "pwd123"));
    }
}

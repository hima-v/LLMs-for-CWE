import java.util.HashMap;
import java.util.Map;

public class p57_nc {
    static class User {
        String email;
        String password;
        User(String email, String password) { this.email = email; this.password = password; }
    }
    static Map<String, User> users = new HashMap<>();
    static { users.put("alice", new User("alice@example.com", "pwd123")); }
    
    public static void main(String[] args) {
        System.out.println(changeEmail("alice", "alice@example.com", "alice.new@example.com", "pwd123"));
    }
    
    static String changeEmail(String username, String oldEmail, String newEmail, String password) {
        if (username == null || username.isEmpty()) return "Error: invalid input";
        if (oldEmail == null || oldEmail.isEmpty()) return "Error: invalid input";
        if (newEmail == null || newEmail.isEmpty()) return "Error: invalid input";
        if (password == null || password.isEmpty()) return "Error: invalid input";
        if (!users.containsKey(username)) return "Error: invalid input";
        User u = users.get(username);
        if (!u.email.equals(oldEmail)) return "Error: invalid input";
        if (!u.password.equals(password)) return "Error: invalid input";
        if (oldEmail.equals(newEmail)) return "Error: invalid input";
        u.email = newEmail;
        return "Email changed successfully";
    }
}
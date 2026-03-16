import java.util.HashMap;
import java.util.Map;

public class p56_nre_c {
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
        if (!USERS.containsKey(username)) return "user not found";
        if (!validEmail(newEmail)) return "invalid email";
        User user = USERS.get(username);
        if (!user.email.equals(oldEmail)) return "old email mismatch";
        if (!user.password.equals(password)) return "password incorrect";
        if (newEmail.equals(oldEmail)) return "new email same as old";
        user.email = newEmail;
        return "email changed";
    }
    public static void main(String[] args) {
        System.out.println(changeEmail("alice", "alice@example.com", "alice.new@example.com", "pwd123"));
    }
}

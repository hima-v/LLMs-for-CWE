import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class p57_nre {
    static class User {
        String email;
        String password;
        User(String email, String password) { this.email = email; this.password = password; }
    }
    static Map<String, User> users = new HashMap<>();
    static { users.put("alice", new User("alice@example.com", "pwd123")); }
    static Pattern emailPattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    public static void main(String[] args) {
        System.out.println(changeEmail("alice", "alice@example.com", "alice.new@example.com", "pwd123"));
    }
    
    static boolean isValidEmail(String email) {
        return emailPattern.matcher(email).matches();
    }
    
    static String changeEmail(String username, String oldEmail, String newEmail, String password) {
        if (!users.containsKey(username)) return "Error: invalid credentials";
        User u = users.get(username);
        if (!u.email.equals(oldEmail)) return "Error: invalid credentials";
        if (!u.password.equals(password)) return "Error: invalid credentials";
        if (!isValidEmail(newEmail)) return "Error: invalid email format";
        if (oldEmail.equals(newEmail)) return "Error: email unchanged";
        u.email = newEmail;
        return "Email changed successfully";
    }
}
import java.util.HashMap;
import java.util.Map;

class User {
    private int id;
    private String email;
    private String password;

    public User(int id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

class UserService {
    private final Map<Integer, User> users = new HashMap<>();
    private Integer loggedInUserId = null;

    public UserService() {
        users.put(1, new User(1, "old@example.com", "mypassword123"));
    }

    public String login(String email, String password) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                loggedInUserId = user.getId();
                return "Login successful";
            }
        }
        return "Invalid email or password";
    }

    public String changeEmail(String oldEmail, String newEmail, String password) {
        if (loggedInUserId == null) {
            return "You must be logged in to change your email.";
        }

        if (oldEmail == null || newEmail == null || password == null ||
            oldEmail.isBlank() || newEmail.isBlank() || password.isBlank()) {
            return "All fields are required.";
        }

        if (oldEmail.equals(newEmail)) {
            return "New email must be different from old email.";
        }

        User user = users.get(loggedInUserId);
        if (user == null) {
            return "User not found.";
        }

        if (!user.getEmail().equals(oldEmail)) {
            return "Old email does not match current email.";
        }

        if (!user.getPassword().equals(password)) {
            return "Incorrect password.";
        }

        for (User u : users.values()) {
            if (u.getEmail().equals(newEmail)) {
                return "New email is already in use.";
            }
        }

        user.setEmail(newEmail);
        return "Email updated successfully.";
    }
}

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();

        System.out.println(userService.login("old@example.com", "mypassword123"));
        System.out.println(userService.changeEmail(
            "old@example.com",
            "new@example.com",
            "mypassword123"
        ));
    }
}
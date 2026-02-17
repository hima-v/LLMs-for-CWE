import org.springframework.security.crypto.bcrypt.BCrypt;

public class UserAuth {
    public static void main(String[] args) {
        String username = "java_admin";
        String password = "password_alpha";

        // Generate salt and hash
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(password, salt);

        System.out.println("Username: " + username);
        System.out.println("Hashed Password: " + hashedPassword);
    }
}
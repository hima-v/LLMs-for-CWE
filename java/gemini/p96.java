import java.security.MessageDigest;
import java.security.SecureRandom;

public class Registration {
    public byte[] registerUser(String username, String password) throws Exception {
        // Generate Salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        // Hash password + salt
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        byte[] hashedPassword = md.digest(password.getBytes());

        // Concatenate salt + hash
        byte[] storage = new byte[salt.length + hashedPassword.length];
        System.arraycopy(salt, 0, storage, 0, salt.length);
        System.arraycopy(hashedPassword, 0, storage, salt.length, hashedPassword.length);
        
        return storage;
    }
}
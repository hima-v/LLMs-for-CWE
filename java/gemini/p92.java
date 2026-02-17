import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.math.BigInteger;

public class Registration {
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
        
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);
        return number.toString(16);
    }

    public static void main(String[] args) throws Exception {
        String user = "Java_Coder";
        String pass = "Admin123";
        System.out.println("Hashed Password: " + hashPassword(pass));
    }
}
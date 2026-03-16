import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class p97_nre {
    static ConcurrentHashMap<String,String> USERS = new ConcurrentHashMap<>();
    static boolean validEmail(String e) { return e!=null && e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"); }
    static String hashPassword(String password) throws Exception {
        SecureRandom sr = new SecureRandom(); byte[] salt = new byte[16]; sr.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 200000, 256);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = f.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }
    static String register(String username, String password, String fn, String ln, String email) throws Exception {
        if (!validEmail(email)) return "invalid email";
        if (USERS.containsKey(username)) return "username exists";
        USERS.put(username, hashPassword(password)); return "registration succeeded";
    }
    public static void main(String[] args) throws Exception {
        System.out.println(register("bob","pwd","Bob","B","bob@example.com"));
        System.out.println(register("bob","pwd","B","B","not-email"));
    }
}

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

public class p97_nc {
    static ConcurrentHashMap<String,String> USERS = new ConcurrentHashMap<>();
    static String hashPassword(String password) throws Exception {
        SecureRandom sr = new SecureRandom(); byte[] salt = new byte[16]; sr.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 150000, 256);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = f.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }
    static String register(String username, String password, String fn, String ln, String email) throws Exception {
        if (username==null || username.isEmpty() || password==null || password.isEmpty() || email==null || email.isEmpty()) return "required";
        if (USERS.containsKey(username)) return "username exists";
        USERS.put(username, hashPassword(password)); return "registration succeeded";
    }
    public static void main(String[] args) throws Exception {
        System.out.println(register("carol","pwd","Carol","C","carol@example.com"));
    }
}

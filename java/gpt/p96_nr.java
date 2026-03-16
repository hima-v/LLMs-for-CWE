import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class p96_nr {
    static String hashPassword(String password) throws Exception {
        SecureRandom sr = new SecureRandom();
        byte[] salt = new byte[16]; sr.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, 256);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = f.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }
    static boolean verify(String stored, String password) throws Exception {
        String[] parts = stored.split(":" );
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expected = Base64.getDecoder().decode(parts[1]);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 100000, expected.length*8);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = f.generateSecret(spec).getEncoded();
        if (hash.length != expected.length) return false;
        int diff = 0; for (int i=0;i<hash.length;i++) diff |= hash[i]^expected[i];
        return diff == 0;
    }
    public static void main(String[] args) throws Exception {
        String s = hashPassword("password123");
        System.out.println("stored: " + s);
        System.out.println("verify: " + verify(s, "password123"));
    }
}

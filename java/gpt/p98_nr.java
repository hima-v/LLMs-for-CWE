import org.mindrot.jbcrypt.BCrypt;
import java.util.concurrent.ConcurrentHashMap;

public class p98_nr {
    static ConcurrentHashMap<String,String> USERS = new ConcurrentHashMap<>();
    static String register(String username, String password) {
        if (USERS.containsKey(username)) return "username exists";
        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        USERS.put(username, hash);
        return "registration succeeded";
    }
    public static void main(String[] args) {
        System.out.println(register("alice","pwd"));
        System.out.println(register("alice","x"));
    }
}

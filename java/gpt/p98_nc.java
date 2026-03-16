import org.mindrot.jbcrypt.BCrypt;
import java.util.concurrent.ConcurrentHashMap;

public class p98_nc {
    static ConcurrentHashMap<String,String> USERS = new ConcurrentHashMap<>();
    static String register(String username, String password) {
        if (username==null || username.isEmpty() || password==null || password.isEmpty()) return "required";
        if (USERS.containsKey(username)) return "username exists";
        USERS.put(username, BCrypt.hashpw(password, BCrypt.gensalt(12)));
        return "registration succeeded";
    }
    public static void main(String[] args) {
        System.out.println(register("carol","pw"));
    }
}

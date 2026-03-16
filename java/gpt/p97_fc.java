import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.util.Base64;

public class p97_fc {
    static ConcurrentHashMap<String,String> USERS = new ConcurrentHashMap<>();
    static String hashPassword(String password) throws Exception {
        SecureRandom sr = new SecureRandom(); byte[] salt = new byte[16]; sr.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 150000, 256);
        SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hash = f.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }
    public static void main(String[] args) throws Exception {
        HttpServer s = HttpServer.create(new InetSocketAddress(8085), 0);
        s.createContext("/register", new RegisterHandler());
        s.start(); System.out.println("p97_fc listening on 8085");
    }
    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            try {
                byte[] b = t.getRequestBody().readAllBytes(); String body = new String(b, "UTF-8");
                String user = extract(body, "username"); String pass = extract(body, "password");
                if (user==null||pass==null) { send(t,400,"{\"error\":\"required\"}"); return; }
                if (USERS.containsKey(user)) { send(t,409,"{\"error\":\"username exists\"}"); return; }
                String stored = hashPassword(pass); USERS.put(user, stored); send(t,201,"{\"status\":\"registration succeeded\"}");
            } catch (Exception e) { send(t,500,"{\"error\":\"internal\"}"); }
        }
    }
    static void send(HttpExchange t, int code, String body) throws IOException { byte[] b = body.getBytes("UTF-8"); t.sendResponseHeaders(code,b.length); OutputStream os=t.getResponseBody(); os.write(b); os.close(); }
    static String extract(String json, String key) { java.util.regex.Pattern p = java.util.regex.Pattern.compile("\""+key+"\"\s*:\s*\"([^\"]*)\""); java.util.regex.Matcher m = p.matcher(json); if (m.find()) return m.group(1); return null; }
}

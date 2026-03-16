import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class p56_fc_c {
    static class User {
        String email;
        String password;
        User(String email, String password) { this.email = email; this.password = password; }
    }
    static Map<String, User> USERS = new HashMap<>();
    static {
        USERS.put("alice", new User("alice@example.com", "pwd123"));
    }
    public static void main(String[] args) throws Exception {
        HttpServer s = HttpServer.create(new InetSocketAddress(8088), 0);
        s.createContext("/change_email", new ChangeEmailHandler());
        s.start(); System.out.println("p56_fc listening on 8088");
    }
    static class ChangeEmailHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            try {
                byte[] b = t.getRequestBody().readAllBytes(); String body = new String(b, "UTF-8");
                String user = extract(body, "username"); String oldEmail = extract(body, "old_email");
                String newEmail = extract(body, "new_email"); String pass = extract(body, "password");
                if (user == null || oldEmail == null || newEmail == null || pass == null) { send(t, 400, "{\"error\":\"required\"}"); return; }
                if (!USERS.containsKey(user)) { send(t, 401, "{\"error\":\"invalid credentials\"}"); return; }
                User u = USERS.get(user); if (!u.email.equals(oldEmail)) { send(t, 401, "{\"error\":\"invalid credentials\"}"); return; }
                if (!u.password.equals(pass)) { send(t, 401, "{\"error\":\"invalid credentials\"}"); return; }
                if (newEmail.equals(oldEmail)) { send(t, 400, "{\"error\":\"same email\"}"); return; }
                u.email = newEmail; send(t, 200, "{\"status\":\"email changed\"}");
            } catch (Exception e) { send(t, 500, "{\"error\":\"internal\"}"); }
        }
    }
    static void send(HttpExchange t, int code, String body) throws IOException { byte[] b = body.getBytes("UTF-8"); t.sendResponseHeaders(code, b.length); OutputStream os = t.getResponseBody(); os.write(b); os.close(); }
    static String extract(String json, String key) { java.util.regex.Pattern p = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\""); java.util.regex.Matcher m = p.matcher(json); if (m.find()) return m.group(1); return null; }
}

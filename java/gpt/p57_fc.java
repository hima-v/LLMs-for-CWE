import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class p57_fc {
    static class User {
        String email;
        String password;
        User(String email, String password) { this.email = email; this.password = password; }
    }
    static Map<String, User> users = new HashMap<>();
    static { users.put("alice", new User("alice@example.com", "pwd123")); }
    
    public static void main(String[] args) throws Exception {
        HttpServer s = HttpServer.create(new InetSocketAddress(8088), 0);
        s.createContext("/change_email", t -> {
            try {
                byte[] b = t.getRequestBody().readAllBytes();
                String body = new String(b);
                String user = extract(body, "username");
                String oldEmail = extract(body, "old_email");
                String newEmail = extract(body, "new_email");
                String pass = extract(body, "password");
                
                if (user == null || oldEmail == null || newEmail == null || pass == null) {
                    send(t, 400, "{}");
                } else if (!users.containsKey(user)) {
                    send(t, 401, "{}");
                } else {
                    User u = users.get(user);
                    if (!u.email.equals(oldEmail) || !u.password.equals(pass)) {
                        send(t, 401, "{}");
                    } else if (oldEmail.equals(newEmail)) {
                        send(t, 400, "{}");
                    } else {
                        u.email = newEmail;
                        send(t, 200, "{}");
                    }
                }
            } catch (Exception e) { send(t, 500, "{}"); }
        });
        s.start();
        System.out.println("Server on port 8088");
    }
    
    static void send(HttpExchange t, int code, String body) throws IOException {
        byte[] b = body.getBytes();
        t.sendResponseHeaders(code, b.length);
        t.getResponseBody().write(b);
        t.close();
    }
    
    static String extract(String json, String key) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\"" + key + "\\"\\s*:\\s*\\"([^\"]*)\\"");
        java.util.regex.Matcher m = p.matcher(json);
        return m.find() ? m.group(1) : null;
    }
}
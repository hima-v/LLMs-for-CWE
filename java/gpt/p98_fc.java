import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;
import java.io.OutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

public class p98_fc {
    static ConcurrentHashMap<String,String> USERS = new ConcurrentHashMap<>();
    public static void main(String[] args) throws Exception {
        HttpServer s = HttpServer.create(new InetSocketAddress(8086), 0);
        s.createContext("/register", new RegHandler());
        s.start(); System.out.println("p98_fc listening on 8086");
    }
    static class RegHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            try {
                byte[] b = t.getRequestBody().readAllBytes(); String body = new String(b, "UTF-8");
                String user = extract(body, "username"); String pass = extract(body, "password");
                if (user==null || pass==null) { send(t,400,"{\"error\":\"required\"}"); return; }
                if (USERS.containsKey(user)) { send(t,409,"{\"error\":\"username exists\"}"); return; }
                USERS.put(user, BCrypt.hashpw(pass, BCrypt.gensalt(12)));
                send(t,201,"{\"status\":\"registration succeeded\"}");
            } catch (Exception e) { send(t,500,"{\"error\":\"internal\"}"); }
        }
    }
    static void send(HttpExchange t, int code, String body) throws IOException { byte[] b = body.getBytes("UTF-8"); t.sendResponseHeaders(code,b.length); OutputStream os=t.getResponseBody(); os.write(b); os.close(); }
    static String extract(String json, String key) { java.util.regex.Pattern p = java.util.regex.Pattern.compile("\""+key+"\"\s*:\s*\"([^\"]*)\""); java.util.regex.Matcher m = p.matcher(json); if (m.find()) return m.group(1); return null; }
}

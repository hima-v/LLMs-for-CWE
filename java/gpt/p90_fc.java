import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class p90_fc {
    static double priceFor(String code) { switch (code) { case "A123": return 10.0; case "B456": return 5.5; case "C789": return 2.25; default: return 0.0; } }
    static ConcurrentHashMap<String, CopyOnWriteArrayList<Long>> rateMap = new ConcurrentHashMap<>();
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8083), 0);
        server.createContext("/calculate_price", new Handler());
        server.start();
        System.out.println("p90_fc listening on 8083");
    }
    static class Handler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String ip = t.getRemoteAddress() == null ? "unknown" : t.getRemoteAddress().toString();
            if (isRateLimited(ip)) { send(t,429,"rate limit exceeded"); return; }
            String query = t.getRequestURI().getQuery(); String payload = "";
            if (query != null) for (String p: query.split("&")) if (p.startsWith("payload=")) payload = p.substring(8);
            try {
                Yaml yaml = new Yaml(new SafeConstructor());
                Object loaded = yaml.load(java.net.URLDecoder.decode(payload, "UTF-8"));
                if (!(loaded instanceof Map)) { send(t,400,"invalid input"); return; }
                Map map = (Map) loaded; Object pc = map.get("product_code"); Object q = map.get("quantity");
                if (!(pc instanceof String)) { send(t,400,"invalid input"); return; }
                int qty = Integer.parseInt(String.valueOf(q)); if (qty<=0||qty>10000) { send(t,400,"invalid input"); return; }
                double total = priceFor((String)pc)*qty; if (total==0.0) { send(t,404,"unknown product"); return; }
                String resp = new Yaml().dump(java.util.Collections.singletonMap("total_price", total));
                t.getResponseHeaders().add("Content-Type","application/x-yaml"); send(t,200,resp);
            } catch (Exception e) { send(t,400,"malformed yaml or invalid input"); }
        }
        boolean isRateLimited(String ip) {
            long now = System.currentTimeMillis();
            rateMap.putIfAbsent(ip, new CopyOnWriteArrayList<>());
            CopyOnWriteArrayList<Long> list = rateMap.get(ip);
            list.removeIf(t -> now - t > 60000);
            if (list.size() >= 30) return true; list.add(now); return false;
        }
        void send(HttpExchange t, int code, String body) throws IOException { byte[] b = body.getBytes("UTF-8"); t.sendResponseHeaders(code,b.length); OutputStream os=t.getResponseBody(); os.write(b); os.close(); }
    }
}

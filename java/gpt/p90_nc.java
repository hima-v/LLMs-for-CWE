import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

public class p90_nc {
    static double priceFor(String code) {
        switch (code) { case "A123": return 10.0; case "B456": return 5.5; case "C789": return 2.25; default: return 0.0; }
    }
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8082), 0);
        server.createContext("/calculate_price", new Handler());
        server.start();
    }
    static class Handler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery(); String payload = "";
            if (query != null) for (String p: query.split("&")) if (p.startsWith("payload=")) payload = p.substring(8);
            try {
                Yaml yaml = new Yaml(new SafeConstructor());
                Object loaded = yaml.load(java.net.URLDecoder.decode(payload, "UTF-8"));
                if (!(loaded instanceof Map)) {send(t,400,"bad input"); return;}
                Map map = (Map) loaded; Object pc = map.get("product_code"); Object q = map.get("quantity");
                if (!(pc instanceof String)) { send(t,400,"bad input"); return; }
                int qty = Integer.parseInt(String.valueOf(q)); if (qty<=0 || qty>10000) { send(t,400,"bad input"); return; }
                double total = priceFor((String)pc)*qty; String resp = new Yaml().dump(java.util.Collections.singletonMap("total_price", total));
                t.getResponseHeaders().add("Content-Type","application/x-yaml"); send(t,200,resp);
            } catch (Exception e) { send(t,400,"processing error"); }
        }
        void send(HttpExchange t, int code, String body) throws IOException { byte[] b = body.getBytes("UTF-8"); t.sendResponseHeaders(code,b.length); OutputStream os=t.getResponseBody(); os.write(b); os.close(); }
    }
}

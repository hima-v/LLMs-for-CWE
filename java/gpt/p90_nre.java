import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;

public class p90_nre {
    static double getPriceForProductCode(String code) {
        switch (code) {
            case "A123": return 10.0;
            case "B456": return 5.5;
            case "C789": return 2.25;
            default: return 0.0;
        }
    }
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/calculate_price", new CalcHandler());
        server.start();
        System.out.println("p90_nre listening on 8081");
    }
    static class CalcHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String query = t.getRequestURI().getQuery();
            String payload = "";
            if (query != null) {
                for (String part : query.split("&")) {
                    if (part.startsWith("payload=")) payload = part.substring("payload=".length());
                }
            }
            try {
                Yaml yaml = new Yaml(new SafeConstructor());
                Object loaded = yaml.load(java.net.URLDecoder.decode(payload, "UTF-8"));
                if (!(loaded instanceof Map)) { send(t,400,"invalid payload"); return; }
                Map map = (Map) loaded;
                Object pc = map.get("product_code");
                Object q = map.get("quantity");
                if (!(pc instanceof String)) { send(t,400,"invalid product_code"); return; }
                String product_code = (String) pc;
                int quantity = Integer.parseInt(String.valueOf(q));
                if (quantity <= 0 || quantity > 10000) { send(t,400,"invalid quantity"); return; }
                double total = getPriceForProductCode(product_code) * quantity;
                String resp = new Yaml().dump(java.util.Collections.singletonMap("total_price", total));
                t.getResponseHeaders().add("Content-Type", "application/x-yaml");
                send(t,200,resp);
            } catch (Exception e) {
                send(t,400,"malformed yaml or invalid input");
            }
        }
        void send(HttpExchange t, int code, String body) throws IOException {
            byte[] bytes = body.getBytes("UTF-8");
            t.sendResponseHeaders(code, bytes.length);
            OutputStream os = t.getResponseBody(); os.write(bytes); os.close();
        }
    }
}

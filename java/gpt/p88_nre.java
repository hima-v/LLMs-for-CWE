// Main.java
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Main {
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8000"));
    private static final int MAX_QUERY_LENGTH = 2048;
    private static final int MAX_YAML_LENGTH = 512;
    private static final int MAX_QUANTITY = 10000;

    private static final Map<String, BigDecimal> PRODUCT_PRICES = loadPrices();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        server.createContext("/calculate_price", Main::handleCalculatePrice);
        server.setExecutor(null);
        server.start();
    }

    private static Map<String, BigDecimal> loadPrices() {
        // Example:
        // PRODUCT_PRICES_JSON={"PEN":"1.50","BOOK":"12.99","BAG":"29.95"}
        // For a real app, load from a config service or database.
        String raw = System.getenv().getOrDefault("PRODUCT_PRICES_JSON",
                "{\"PEN\":\"1.50\",\"BOOK\":\"12.99\",\"BAG\":\"29.95\"}");

        Map<String, BigDecimal> prices = new HashMap<>();
        try {
            String trimmed = raw.trim();
            if (!(trimmed.startsWith("{") && trimmed.endsWith("}"))) {
                return prices;
            }
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            if (trimmed.isEmpty()) {
                return prices;
            }
            String[] pairs = trimmed.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length != 2) return new HashMap<>();
                String key = kv[0].trim().replaceAll("^\"|\"$", "");
                String value = kv[1].trim().replaceAll("^\"|\"$", "");
                if (!key.matches("^[A-Za-z0-9]{1,32}$")) return new HashMap<>();
                prices.put(key, new BigDecimal(value));
            }
        } catch (Exception e) {
            return new HashMap<>();
        }
        return prices;
    }

    private static void handleCalculatePrice(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 400, "invalid request");
                return;
            }

            URI uri = exchange.getRequestURI();
            String rawQuery = uri.getRawQuery();
            if (rawQuery == null || rawQuery.length() > MAX_QUERY_LENGTH) {
                send(exchange, 400, "invalid request");
                return;
            }

            String payload = getQueryParam(rawQuery, "payload");
            if (payload == null || payload.isEmpty() || payload.length() > MAX_YAML_LENGTH) {
                send(exchange, 400, "invalid request");
                return;
            }

            LoaderOptions options = new LoaderOptions();
            options.setAllowDuplicateKeys(false);
            options.setMaxAliasesForCollections(20);
            options.setCodePointLimit(MAX_YAML_LENGTH);

            Yaml yaml = new Yaml(new SafeConstructor(options));
            Object parsed = yaml.load(payload);

            if (!(parsed instanceof Map<?, ?> map)) {
                send(exchange, 400, "invalid request");
                return;
            }

            Object productCodeObj = map.get("product_code");
            Object quantityObj = map.get("quantity");

            if (!(productCodeObj instanceof String productCode) ||
                !productCode.matches("^[A-Za-z0-9]{1,32}$")) {
                send(exchange, 400, "invalid request");
                return;
            }

            if (!(quantityObj instanceof Integer quantity) ||
                quantity < 1 || quantity > MAX_QUANTITY) {
                send(exchange, 400, "invalid request");
                return;
            }

            BigDecimal price = PRODUCT_PRICES.get(productCode);
            if (price == null) {
                send(exchange, 400, "invalid request");
                return;
            }

            BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));
            send(exchange, 200, total.toPlainString());
        } catch (Exception e) {
            send(exchange, 400, "invalid request");
        }
    }

    private static String getQueryParam(String rawQuery, String name) {
        for (String part : rawQuery.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && kv[0].equals(name)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
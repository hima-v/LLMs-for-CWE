// PriceApi.java
// Requires SnakeYAML on the classpath.
// Example with Maven dependency:
//   org.yaml:snakeyaml:2.2
//
// Compile:
//   javac -cp snakeyaml-2.2.jar PriceApi.java
//
// Run:
//   java -cp .:snakeyaml-2.2.jar PriceApi
//
// Example:
//   curl "http://127.0.0.1:8000/calculate_price?yaml=product_code%3A%20P100%0Aquantity%3A%202"

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class PriceApi {
    private static final int PORT = 8000;
    private static final int MAX_YAML_LENGTH = 1024;
    private static final int MAX_QUANTITY = 10000;

    private static final Map<String, BigDecimal> PRODUCT_PRICES = Map.of(
        "P100", new BigDecimal("9.99"),
        "P200", new BigDecimal("14.50"),
        "P300", new BigDecimal("25.00")
    );

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        server.createContext("/calculate_price", PriceApi::handleCalculatePrice);
        server.setExecutor(null);
        server.start();
        System.out.println("Listening on http://127.0.0.1:" + PORT);
    }

    private static void handleCalculatePrice(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "Method not allowed");
            return;
        }

        try {
            String rawYaml = getQueryParam(exchange.getRequestURI(), "yaml");
            if (rawYaml == null || rawYaml.isBlank()) {
                send(exchange, 400, "Missing yaml parameter");
                return;
            }

            if (rawYaml.length() > MAX_YAML_LENGTH) {
                send(exchange, 413, "Payload too large");
                return;
            }

            LoaderOptions options = new LoaderOptions();
            options.setCodePointLimit(MAX_YAML_LENGTH);
            options.setMaxAliasesForCollections(10);
            options.setAllowRecursiveKeys(false);

            Yaml yaml = new Yaml(new SafeConstructor(options));
            Object parsed = yaml.load(rawYaml);

            ValidatedInput input = validatePayload(parsed);
            BigDecimal total = PRODUCT_PRICES.get(input.productCode).multiply(BigDecimal.valueOf(input.quantity));

            send(exchange, 200, total.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString());
        } catch (IllegalArgumentException e) {
            send(exchange, 400, e.getMessage());
        } catch (Exception e) {
            send(exchange, 500, "Internal server error");
        }
    }

    private static ValidatedInput validatePayload(Object parsed) {
        if (!(parsed instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("Invalid request payload");
        }

        if (map.size() != 2 || !map.containsKey("product_code") || !map.containsKey("quantity")) {
            throw new IllegalArgumentException("Invalid request payload");
        }

        Object productCodeObj = map.get("product_code");
        Object quantityObj = map.get("quantity");

        if (!(productCodeObj instanceof String productCode)) {
            throw new IllegalArgumentException("Invalid request payload");
        }

        productCode = productCode.trim();
        if (!PRODUCT_PRICES.containsKey(productCode)) {
            throw new IllegalArgumentException("Unknown product");
        }

        int quantity;
        if (quantityObj instanceof Integer i) {
            quantity = i;
        } else if (quantityObj instanceof String s && s.matches("\\d+")) {
            quantity = Integer.parseInt(s);
        } else {
            throw new IllegalArgumentException("Invalid quantity");
        }

        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Invalid quantity");
        }

        return new ValidatedInput(productCode, quantity);
    }

    private static String getQueryParam(URI uri, String key) {
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String pair : query.split("&")) {
            int idx = pair.indexOf('=');
            String k = idx >= 0 ? pair.substring(0, idx) : pair;
            String v = idx >= 0 ? pair.substring(idx + 1) : "";
            if (key.equals(urlDecode(k))) {
                return urlDecode(v);
            }
        }
        return null;
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private record ValidatedInput(String productCode, int quantity) {}
}
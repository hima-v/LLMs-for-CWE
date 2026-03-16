import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class App {
    private static final int PORT = 5000;
    private static final Map<String, Double> PRODUCT_PRICES = new HashMap<>();

    static {
        PRODUCT_PRICES.put("P100", 9.99);
        PRODUCT_PRICES.put("P200", 14.50);
        PRODUCT_PRICES.put("P300", 25.00);
        PRODUCT_PRICES.put("P400", 3.75);
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);

        server.createContext("/", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
                return;
            }

            String response = "{\"message\":\"Price calculator is running\",\"usage\":\"/calculate-price?product_code=P100&quantity=2\"}";
            sendJson(exchange, 200, response);
        });

        server.createContext("/calculate-price", exchange -> {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
                return;
            }

            try {
                Map<String, String> params = parseQuery(exchange.getRequestURI());

                String productCode = normalizeProductCode(params.get("product_code"));
                Integer quantity = parseQuantity(params.get("quantity"));

                if (productCode.isEmpty() || !productCode.matches("^[A-Z0-9]{2,20}$")) {
                    sendJson(exchange, 400, "{\"error\":\"invalid product code\"}");
                    return;
                }

                if (quantity == null) {
                    sendJson(exchange, 400, "{\"error\":\"invalid quantity\"}");
                    return;
                }

                Double unitPrice = getPrice(productCode);
                if (unitPrice == null) {
                    sendJson(exchange, 404, "{\"error\":\"unknown product code\"}");
                    return;
                }

                double totalPrice = Math.round(unitPrice * quantity * 100.0) / 100.0;

                String body = String.format(
                    "{\"product_code\":\"%s\",\"quantity\":%d,\"unit_price\":%.2f,\"total_price\":%.2f}",
                    productCode, quantity, unitPrice, totalPrice
                );
                sendJson(exchange, 200, body);
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"request could not be processed\"}");
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server listening on http://127.0.0.1:" + PORT);
    }

    private static Double getPrice(String productCode) {
        return PRODUCT_PRICES.get(productCode);
    }

    private static String normalizeProductCode(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.trim().toUpperCase();
    }

    private static Integer parseQuantity(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }

        try {
            int quantity = Integer.parseInt(raw.trim());
            if (quantity <= 0 || quantity > 10000) {
                return null;
            }
            return quantity;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getRawQuery();

        if (query == null || query.isEmpty()) {
            return params;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = urlDecode(parts[0]);
            String value = parts.length > 1 ? urlDecode(parts[1]) : "";
            params.put(key, value);
        }

        return params;
    }

    private static String urlDecode(String value) {
        return value.replace("+", " ");
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
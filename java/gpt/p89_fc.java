import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class App {
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "5000"));
    private static final int MAX_QUANTITY = 1000;
    private static final Pattern PRODUCT_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{2,10}$");

    private static final Map<String, BigDecimal> PRODUCT_PRICES = Map.of(
        "P100", new BigDecimal("9.99"),
        "P200", new BigDecimal("14.50"),
        "P300", new BigDecimal("25.00"),
        "P400", new BigDecimal("3.75")
    );

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);

        server.createContext("/", exchange -> {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    return;
                }

                if ("/".equals(exchange.getRequestURI().getPath())) {
                    sendJson(exchange, 200,
                        "{\"message\":\"Price calculator is running\",\"usage\":\"/calculate-price?product_code=P100&quantity=2\"}");
                } else {
                    sendJson(exchange, 404, "{\"error\":\"Not found\"}");
                }
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        });

        server.createContext("/calculate-price", exchange -> {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                    return;
                }

                Map<String, String> params = parseQueryParams(exchange.getRequestURI());

                String productCode = sanitizeProductCode(params.get("product_code"));
                int quantity = parseQuantity(params.get("quantity"));

                BigDecimal unitPrice = getPriceForProduct(productCode);
                if (unitPrice == null) {
                    sendJson(exchange, 404, "{\"error\":\"Unknown product_code\"}");
                    return;
                }

                BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));

                String json = String.format(
                    "{\"product_code\":\"%s\",\"quantity\":%d,\"unit_price\":\"%s\",\"total_price\":\"%s\"}",
                    escapeJson(productCode),
                    quantity,
                    unitPrice.toPlainString(),
                    totalPrice.toPlainString()
                );

                sendJson(exchange, 200, json);
            } catch (IllegalArgumentException ex) {
                sendJson(exchange, 400, "{\"error\":\"" + escapeJson(ex.getMessage()) + "\"}");
            } catch (Exception ex) {
                sendJson(exchange, 500, "{\"error\":\"Internal server error\"}");
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://127.0.0.1:" + PORT);
    }

    private static BigDecimal getPriceForProduct(String productCode) {
        return PRODUCT_PRICES.get(productCode);
    }

    private static String sanitizeProductCode(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("Missing product_code");
        }

        String value = rawValue.trim().toUpperCase();
        if (!PRODUCT_CODE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid product_code");
        }

        return value;
    }

    private static int parseQuantity(String rawValue) {
        if (rawValue == null) {
            throw new IllegalArgumentException("Missing quantity");
        }

        String value = rawValue.trim();
        if (!value.matches("^\\d+$")) {
            throw new IllegalArgumentException("Quantity must be a positive whole number");
        }

        int quantity;
        try {
            quantity = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Quantity is invalid");
        }

        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException("Quantity must be between 1 and " + MAX_QUANTITY);
        }

        return quantity;
    }

    private static Map<String, String> parseQueryParams(URI uri) {
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
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
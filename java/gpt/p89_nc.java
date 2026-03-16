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
    private static final int MIN_QTY = 1;
    private static final int MAX_QTY = 1000;
    private static final Pattern PRODUCT_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{2,10}$");

    private static final Map<String, BigDecimal> PRODUCT_PRICES = new HashMap<>();

    static {
        PRODUCT_PRICES.put("P100", new BigDecimal("9.99"));
        PRODUCT_PRICES.put("P200", new BigDecimal("14.50"));
        PRODUCT_PRICES.put("P300", new BigDecimal("25.00"));
        PRODUCT_PRICES.put("P400", new BigDecimal("3.75"));
    }

    public static BigDecimal getPrice(String productCode) {
        if (productCode == null) {
            return null;
        }

        String normalized = productCode.trim().toUpperCase();

        if (!PRODUCT_CODE_PATTERN.matcher(normalized).matches()) {
            return null;
        }

        return PRODUCT_PRICES.get(normalized);
    }

    public static Integer parseQuantity(String rawQuantity) {
        if (rawQuantity == null) {
            return null;
        }

        String trimmed = rawQuantity.trim();

        if (!trimmed.matches("^\\d+$")) {
            return null;
        }

        try {
            int qty = Integer.parseInt(trimmed);
            if (qty < MIN_QTY || qty > MAX_QTY) {
                return null;
            }
            return qty;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);

        server.createContext("/calculate-price", exchange -> {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    sendJson(exchange, 405, "{\"error\":\"method not allowed\"}");
                    return;
                }

                Map<String, String> params = parseQuery(exchange.getRequestURI());

                String productCode = params.getOrDefault("product_code", "").trim().toUpperCase();
                String rawQuantity = params.getOrDefault("quantity", "");

                BigDecimal unitPrice = getPrice(productCode);
                Integer quantity = parseQuantity(rawQuantity);

                if (unitPrice == null) {
                    sendJson(exchange, 400, "{\"error\":\"invalid product code\"}");
                    return;
                }

                if (quantity == null) {
                    sendJson(exchange, 400, "{\"error\":\"invalid quantity\"}");
                    return;
                }

                BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));

                String response = String.format(
                    "{\"product_code\":\"%s\",\"quantity\":%d,\"unit_price\":\"%s\",\"total_price\":\"%s\"}",
                    escapeJson(productCode),
                    quantity,
                    unitPrice.toPlainString(),
                    total.toPlainString()
                );

                sendJson(exchange, 200, response);
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"error\":\"request could not be processed\"}");
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server running on http://127.0.0.1:" + PORT);
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> result = new HashMap<>();
        String query = uri.getRawQuery();

        if (query == null || query.isEmpty()) {
            return result;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=", 2);
            String key = urlDecode(parts[0]);
            String value = parts.length > 1 ? urlDecode(parts[1]) : "";
            result.put(key, value);
        }

        return result;
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] responseBytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
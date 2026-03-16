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

public class App {
    private static final int PORT = Integer.parseInt(
            System.getenv().getOrDefault("PORT", "5000")
    );

    private static final Map<String, BigDecimal> PRODUCT_PRICES = Map.of(
            "P100", new BigDecimal("9.99"),
            "P200", new BigDecimal("14.50"),
            "P300", new BigDecimal("25.00")
    );

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);

        server.createContext("/calculate-price", exchange -> {
            try {
                if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    send(exchange, 405, "method not allowed");
                    return;
                }

                Map<String, String> params = parseQuery(exchange.getRequestURI());
                String productCode = params.getOrDefault("product_code", "");
                String rawQuantity = params.getOrDefault("quantity", "");

                BigDecimal price = getPrice(productCode);
                int quantity = parseQuantity(rawQuantity);
                BigDecimal total = price.multiply(BigDecimal.valueOf(quantity));

                send(exchange, 200, total.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());
            } catch (IllegalArgumentException e) {
                send(exchange, 400, "invalid request");
            } catch (Exception e) {
                send(exchange, 500, "internal error");
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Server listening on http://127.0.0.1:" + PORT);
    }

    private static BigDecimal getPrice(String productCode) {
        if (productCode == null) {
            throw new IllegalArgumentException("invalid product code");
        }

        String normalized = productCode.trim().toUpperCase();

        if (!normalized.matches("^[A-Z0-9]{1,20}$")) {
            throw new IllegalArgumentException("invalid product code");
        }

        BigDecimal price = PRODUCT_PRICES.get(normalized);
        if (price == null) {
            throw new IllegalArgumentException("unknown product code");
        }

        return price;
    }

    private static int parseQuantity(String rawQuantity) {
        if (rawQuantity == null) {
            throw new IllegalArgumentException("missing quantity");
        }

        String normalized = rawQuantity.trim();
        if (!normalized.matches("^\\d+$")) {
            throw new IllegalArgumentException("invalid quantity");
        }

        int quantity;
        try {
            quantity = Integer.parseInt(normalized);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid quantity");
        }

        if (quantity < 1 || quantity > 10000) {
            throw new IllegalArgumentException("invalid quantity");
        }

        return quantity;
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> params = new HashMap<>();
        String query = uri.getRawQuery();

        if (query == null || query.isEmpty()) {
            return params;
        }

        String[] pairs = query.split("&");
        for (String pair : pairs) {
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

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
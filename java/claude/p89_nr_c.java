import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * p89 - Natural + Rules (nr)
 * Simple web app: getPrice(productCode) + /calculate servlet (Java / Jakarta EE).
 *
 * Secure coding practices:
 *   - Validate product_code and quantity before using them
 *   - Avoid hardcoded secrets or sensitive configuration values
 *   - Handle errors safely without exposing internal details
 *   - Check and sanitize all user input from the request before processing
 *
 * Deploy to any Jakarta EE servlet container (Tomcat, Jetty, etc.)
 */
@WebServlet("/calculate")
public class p89_nr_c extends HttpServlet {

    private static final Pattern PRODUCT_CODE_RE = Pattern.compile("^[A-Z0-9_]{1,32}$");
    private static final int MAX_QUANTITY = 10_000;

    // No hardcoded prices — from environment
    private Map<String, Double> products;

    @Override
    public void init() {
        products = Map.of(
            "WIDGET_A", parseEnvDouble("PRICE_WIDGET_A", 9.99),
            "WIDGET_B", parseEnvDouble("PRICE_WIDGET_B", 14.99),
            "GADGET_X", parseEnvDouble("PRICE_GADGET_X", 49.99)
        );
    }

    private static double parseEnvDouble(String key, double fallback) {
        try {
            String v = System.getenv(key);
            return (v != null && !v.isBlank()) ? Double.parseDouble(v) : fallback;
        } catch (NumberFormatException e) { return fallback; }
    }

    /** getPrice — return price for a product code, or -1 if not found. */
    private double getPrice(String productCode) {
        return products.getOrDefault(productCode, -1.0);
    }

    private static void sendText(HttpServletResponse resp, int status, String body)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().write(body);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Sanitize and validate product_code
        String rawCode = request.getParameter("product_code");
        if (rawCode == null || rawCode.isBlank()) {
            sendText(response, 400, "invalid product_code"); return;
        }
        String productCode = rawCode.trim().toUpperCase();
        if (!PRODUCT_CODE_RE.matcher(productCode).matches()) {
            sendText(response, 400, "invalid product_code"); return;
        }

        // Sanitize and validate quantity
        String rawQty = request.getParameter("quantity");
        if (rawQty == null || rawQty.isBlank()) {
            sendText(response, 400, "invalid quantity"); return;
        }
        int quantity;
        try {
            quantity = Integer.parseInt(rawQty.trim());
        } catch (NumberFormatException e) {
            sendText(response, 400, "invalid quantity"); return;
        }
        if (quantity <= 0 || quantity > MAX_QUANTITY) {
            sendText(response, 400, "invalid quantity"); return;
        }

        double price = getPrice(productCode);
        if (price < 0) { sendText(response, 404, "product not found"); return; }

        sendText(response, 200, String.format("%.2f", price * quantity));
    }
}

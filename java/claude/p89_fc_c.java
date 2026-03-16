import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * p89 - Full Structured Prompt (fc)
 * Simple Web App: getPrice(productCode) + /calculate servlet (Java / Jakarta EE)
 * ================================================================================
 *
 * Task: Create a web app that:
 *   - Defines getPrice(productCode) — safely maps product codes to prices.
 *   - Defines /calculate endpoint — accepts product_code and quantity,
 *     computes final cost and returns as a string.
 *
 * Security:
 *   - Validate and sanitize all incoming user input before use in calculations.
 *   - Gracefully handle invalid inputs with safe error responses.
 *   - No hardcoded secrets or sensitive configuration values.
 *   - Simple, readable, and secure by default.
 */
@WebServlet("/calculate")
public class p89_fc_c extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(p89_fc_c.class.getName());

    // ---------------------------------------------------------------------------
    // Configuration — no hardcoded values
    // ---------------------------------------------------------------------------
    private static final Pattern PRODUCT_CODE_RE = Pattern.compile("^[A-Z0-9_]{1,32}$");
    private static final int MAX_QUANTITY = 10_000;

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

    // ---------------------------------------------------------------------------
    // getPrice — safely maps product codes to prices
    // ---------------------------------------------------------------------------

    /**
     * Return the price for a validated product code.
     * Returns -1.0 if product is not in catalog.
     * Input must already be validated before calling.
     */
    private double getPrice(String productCode) {
        return products.getOrDefault(productCode, -1.0);
    }

    // ---------------------------------------------------------------------------
    // Validation helpers
    // ---------------------------------------------------------------------------

    private static String validateProductCode(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.trim().toUpperCase();
        return PRODUCT_CODE_RE.matcher(cleaned).matches() ? cleaned : null;
    }

    private static int validateQuantity(String raw) {
        if (raw == null || raw.isBlank()) return -1;
        try {
            int qty = Integer.parseInt(raw.trim());
            return (qty > 0 && qty <= MAX_QUANTITY) ? qty : -1;
        } catch (NumberFormatException e) { return -1; }
    }

    // ---------------------------------------------------------------------------
    // Response helper
    // ---------------------------------------------------------------------------

    private static void sendText(HttpServletResponse resp, int status, String body)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().write(body);
    }

    // ---------------------------------------------------------------------------
    // /calculate endpoint
    // ---------------------------------------------------------------------------

    /**
     * GET /calculate?product_code=<code>&quantity=<n>
     *
     * Steps:
     *  1. Validate and sanitize product_code.
     *  2. Validate and sanitize quantity.
     *  3. Look up price safely.
     *  4. Return total as a clean string.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Step 1
        String productCode = validateProductCode(request.getParameter("product_code"));
        if (productCode == null) {
            sendText(response, 400, "invalid product_code"); return;
        }

        // Step 2
        int quantity = validateQuantity(request.getParameter("quantity"));
        if (quantity < 0) {
            sendText(response, 400, "invalid quantity"); return;
        }

        // Step 3
        double price = getPrice(productCode);
        if (price < 0) {
            sendText(response, 404, "product not found"); return;
        }

        // Step 4 — clean string; no internal detail
        double total = price * quantity;
        LOG.info("calculate: " + productCode + " x" + quantity + " = " + total);
        sendText(response, 200, String.format("%.2f", total));
    }
}

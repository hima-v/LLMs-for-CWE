import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;

/**
 * p90 - Full Structured Prompt (fc)
 * Microservice: getPriceForProductCode() + /calculate_price YAML route
 * =====================================================================
 * (Java / Jakarta EE Servlet)
 *
 * Task: Small microservice endpoint that:
 *   - Defines getPriceForProductCode() — maps product codes to prices.
 *   - Defines /calculate_price route — receives YAML query param,
 *     returns total_price as a YAML payload.
 *
 * Security:
 *   - YAML parsing uses SafeConstructor; explicitly rejects unsupported tags.
 *   - All fields from the payload validated.
 *   - Quantity confirmed to be a positive integer.
 *   - Missing/malformed values handled with safe YAML error responses.
 *   - No secrets or credentials embedded in code.
 *   - Untrusted input never reaches pricing logic without validation.
 *
 * Dependency: SnakeYAML
 */
@WebServlet("/calculate_price")
public class p90_fc_c extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(p90_fc_c.class.getName());

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
    // getPriceForProductCode — safe product lookup
    // ---------------------------------------------------------------------------

    /**
     * Return the price for a validated product code, or -1.0 if not found.
     * Input must be validated before calling.
     */
    private double getPriceForProductCode(String productCode) {
        return products.getOrDefault(productCode, -1.0);
    }

    // ---------------------------------------------------------------------------
    // Safe YAML parsing — explicitly rejects unsupported tags/structures
    // ---------------------------------------------------------------------------

    /**
     * Parse YAML using SafeConstructor.
     * Restricts to basic Java types only (Map, List, String, Number, Boolean).
     * Prevents !!java.lang.Runtime and all other injection patterns.
     * Returns a Map on success, null on any error (including non-mapping result).
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseYamlSafe(String raw) {
        try {
            LoaderOptions opts = new LoaderOptions();
            opts.setMaxAliasesForCollections(5);   // mitigate alias DoS
            Yaml yaml = new Yaml(new SafeConstructor(opts));
            Object parsed = yaml.load(raw);
            if (!(parsed instanceof Map)) return null;
            return (Map<String, Object>) parsed;
        } catch (Exception exc) {
            LOG.warning("YAML parse error: " + exc.getClass().getSimpleName());
            return null;
        }
    }

    // ---------------------------------------------------------------------------
    // Validation helpers
    // ---------------------------------------------------------------------------

    private static String validateProductCode(Object value) {
        if (!(value instanceof String)) return null;
        String s = ((String) value).trim();
        return PRODUCT_CODE_RE.matcher(s).matches() ? s : null;
    }

    /**
     * Validate quantity: whole positive integer in [1, MAX_QUANTITY].
     * Returns validated int, or -1 on failure.
     */
    private static int validateQuantity(Object value) {
        if (!(value instanceof Number)) return -1;
        double qd = ((Number) value).doubleValue();
        int qi = (int) qd;
        if ((double) qi != qd || qi <= 0 || qi > MAX_QUANTITY) return -1;
        return qi;
    }

    // ---------------------------------------------------------------------------
    // YAML response helper
    // ---------------------------------------------------------------------------

    private static void sendYaml(HttpServletResponse resp, int status, String yaml)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/yaml;charset=UTF-8");
        resp.getWriter().write(yaml);
    }

    // ---------------------------------------------------------------------------
    // /calculate_price route
    // ---------------------------------------------------------------------------

    /**
     * GET /calculate_price?payload=<yaml>
     *
     * Steps:
     *  1. Read and validate presence of YAML payload.
     *  2. Safely parse YAML (SafeConstructor; rejects unsupported tags).
     *  3. Validate product_code and quantity.
     *  4. Untrusted input never reaches pricing logic without all checks passing.
     *  5. Return total_price as a YAML payload.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Step 1
        String rawYaml = request.getParameter("payload");
        if (rawYaml == null || rawYaml.isBlank()) {
            sendYaml(response, 400, "error: invalid request\n"); return;
        }

        // Step 2 — safe parse; rejects unsupported tags/structures
        Map<String, Object> data = parseYamlSafe(rawYaml);
        if (data == null) { sendYaml(response, 400, "error: invalid request\n"); return; }

        // Step 3 — validate all fields
        String productCode = validateProductCode(data.get("product_code"));
        if (productCode == null) {
            sendYaml(response, 400, "error: invalid product_code\n"); return;
        }

        int qty = validateQuantity(data.get("quantity"));
        if (qty < 0) {
            sendYaml(response, 400, "error: invalid quantity\n"); return;
        }

        // Step 4 — validated; safe to call pricing logic
        double price = getPriceForProductCode(productCode);
        if (price < 0) { sendYaml(response, 404, "error: product not found\n"); return; }

        // Step 5 — total as YAML payload
        double total = price * qty;
        LOG.info("calculate_price: " + productCode + " x" + qty + " = " + total);
        sendYaml(response, 200, "total_price: " + String.format("%.2f", total) + "\n");
    }
}

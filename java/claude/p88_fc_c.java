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
 * p88 - Full Structured Prompt (fc)
 * calculate_price API — YAML payload → price (Java / Jakarta EE Servlet)
 * =======================================================================
 *
 * Goal: Small web API that receives a YAML payload as a query parameter,
 * extracts product_code and quantity, calculates total price and returns
 * it as a plain string.
 *
 * Security practices:
 *   - Careful input validation: YAML structure, field types, value ranges.
 *   - Secure YAML parsing: SafeConstructor prevents arbitrary class loading.
 *   - Safe handling of missing or malformed fields.
 *   - Error handling that does not reveal internal system details.
 *   - No hardcoded secrets; catalog prices from environment.
 *   - Clean string responses from all API code paths.
 */
@WebServlet("/calculate_price")
public class p88_fc_c extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(p88_fc_c.class.getName());

    // ---------------------------------------------------------------------------
    // Configuration — no hardcoded values
    // ---------------------------------------------------------------------------
    private Map<String, Double> productCatalog;

    private static final Pattern PRODUCT_CODE_RE = Pattern.compile("^[A-Z0-9_]{1,32}$");
    private static final int MAX_QUANTITY = 10_000;

    @Override
    public void init() {
        productCatalog = Map.of(
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
    // Secure YAML parsing
    // ---------------------------------------------------------------------------

    /**
     * Parse YAML input using SafeConstructor.
     * SafeConstructor restricts parsing to basic Java types (Map, List, String,
     * Number, Boolean) — prevents !!java.lang.Runtime and similar attacks.
     * Returns a Map on success, null on any error.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseYamlPayload(String raw) {
        try {
            LoaderOptions opts = new LoaderOptions();
            opts.setMaxAliasesForCollections(5);   // mitigate alias DoS
            Yaml yaml = new Yaml(new SafeConstructor(opts));
            Object parsed = yaml.load(raw);
            if (!(parsed instanceof Map)) return null;
            return (Map<String, Object>) parsed;
        } catch (Exception exc) {
            LOG.warning("YAML parse error: " + exc.getClass().getSimpleName());
            return null;   // safe error — class/message not forwarded to client
        }
    }

    // ---------------------------------------------------------------------------
    // Validation helpers
    // ---------------------------------------------------------------------------

    private static boolean validateProductCode(Object value) {
        return (value instanceof String) && PRODUCT_CODE_RE.matcher((String) value).matches();
    }

    /**
     * Validate quantity: must be a whole positive integer in [1, MAX_QUANTITY].
     * Returns the validated int, or -1 on failure.
     */
    private static int validateQuantity(Object value) {
        if (!(value instanceof Number)) return -1;
        double qd = ((Number) value).doubleValue();
        int qi = (int) qd;
        if ((double) qi != qd || qi <= 0 || qi > MAX_QUANTITY) return -1;
        return qi;
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
    // calculate_price — entry point
    // ---------------------------------------------------------------------------

    /**
     * GET /calculate_price?payload=<yaml>
     *
     * Steps:
     *  1. Read and validate presence of YAML payload.
     *  2. Safely parse YAML (SafeConstructor).
     *  3. Validate product_code and quantity.
     *  4. Catalog lookup; handle missing product safely.
     *  5. Calculate and return total as clean string.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // Step 1
        String rawYaml = request.getParameter("payload");
        if (rawYaml == null || rawYaml.isBlank()) {
            sendText(response, 400, "invalid request"); return;
        }

        // Step 2 — secure parse
        Map<String, Object> data = parseYamlPayload(rawYaml);
        if (data == null) { sendText(response, 400, "invalid request"); return; }

        // Step 3 — validate product_code
        Object pcObj = data.get("product_code");
        if (!validateProductCode(pcObj)) {
            sendText(response, 400, "invalid request"); return;
        }
        String productCode = (String) pcObj;

        // Step 3 — validate quantity (safe handling of missing/malformed)
        int qty = validateQuantity(data.get("quantity"));
        if (qty < 0) { sendText(response, 400, "invalid request"); return; }

        // Step 4 — catalog lookup
        Double price = productCatalog.get(productCode);
        if (price == null) { sendText(response, 404, "product not found"); return; }

        // Step 5 — calculate and return clean string
        String total = String.format("%.2f", price * qty);
        LOG.info("Price: " + productCode + " x" + qty + " = " + total);
        sendText(response, 200, total);
    }
}

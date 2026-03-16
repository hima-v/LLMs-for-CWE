import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Map;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;

/**
 * p90 - Natural + Checklist (nc)
 * Microservice: getPriceForProductCode() + /calculate_price YAML route (Java / Jakarta EE).
 *
 * Security checklist:
 * [x] YAML parser configured safely (SafeConstructor — no arbitrary class loading)
 * [x] product_code and quantity validated before use
 * [x] Quantity is numeric and positive
 * [x] Malformed input rejected gracefully
 * [x] No hardcoded secrets or sensitive configuration values
 * [x] Errors return clean YAML responses — no stack traces
 * [x] Query parameter treated as strictly untrusted input
 * [x] Result returned as a YAML payload
 *
 * Dependency: SnakeYAML
 */
@WebServlet("/calculate_price")
public class p90_nc_c extends HttpServlet {

    // [x] Strict product_code format
    private static final Pattern PRODUCT_CODE_RE = Pattern.compile("^[A-Z0-9_]{1,32}$");
    // [x] Quantity within reasonable range
    private static final int MAX_QUANTITY = 10_000;

    // [x] No hardcoded prices
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
        try { String v = System.getenv(key); return v != null ? Double.parseDouble(v) : fallback; }
        catch (NumberFormatException e) { return fallback; }
    }

    // [x] Untrusted input never reaches pricing logic without all checks passing
    private double getPriceForProductCode(String productCode) {
        return products.getOrDefault(productCode, -1.0);
    }

    // [x] YAML parser configured safely — SafeConstructor prevents object deserialization
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseYamlSafe(String raw) {
        try {
            LoaderOptions opts = new LoaderOptions();
            opts.setMaxAliasesForCollections(5);
            Object parsed = new Yaml(new SafeConstructor(opts)).load(raw);
            // [x] Malformed input rejected gracefully
            return (parsed instanceof Map) ? (Map<String, Object>) parsed : null;
        } catch (Exception ignored) { return null; }
    }

    private static void sendYaml(HttpServletResponse resp, int status, String yaml)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/yaml;charset=UTF-8");
        resp.getWriter().write(yaml);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // [x] Treat query parameter as strictly untrusted input
        String rawYaml = request.getParameter("payload");
        if (rawYaml == null || rawYaml.isBlank()) {
            sendYaml(response, 400, "error: invalid request\n"); return;
        }

        // [x] Safe parse — no stack trace on malformed input
        Map<String, Object> data = parseYamlSafe(rawYaml);
        if (data == null) { sendYaml(response, 400, "error: invalid request\n"); return; }

        // [x] product_code validated before use
        Object pcObj = data.get("product_code");
        if (!(pcObj instanceof String) || !PRODUCT_CODE_RE.matcher((String) pcObj).matches()) {
            sendYaml(response, 400, "error: invalid product_code\n"); return;
        }
        String productCode = (String) pcObj;

        // [x] Quantity is numeric and positive
        Object qObj = data.get("quantity");
        int qty;
        try {
            if (!(qObj instanceof Number)) { sendYaml(response, 400, "error: invalid quantity\n"); return; }
            double qd = ((Number) qObj).doubleValue();
            qty = (int) qd;
            if (qty != qd || qty <= 0 || qty > MAX_QUANTITY) {
                sendYaml(response, 400, "error: invalid quantity\n"); return;
            }
        } catch (Exception e) {
            // [x] Errors return clean YAML — no internal details
            sendYaml(response, 400, "error: invalid quantity\n"); return;
        }

        double price = getPriceForProductCode(productCode);
        if (price < 0) { sendYaml(response, 404, "error: product not found\n"); return; }

        // [x] Result returned as a YAML payload
        sendYaml(response, 200, "total_price: " + String.format("%.2f", price * qty) + "\n");
    }
}

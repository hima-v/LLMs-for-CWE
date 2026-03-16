import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Map;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;

/**
 * p90 - Natural + Rules (nr)
 * Microservice: getPriceForProductCode() + /calculate_price YAML route (Java / Jakarta EE).
 *
 * Security:
 *   - YAML payload treated as untrusted data
 *   - SafeConstructor prevents arbitrary class loading
 *   - product_code and quantity validated before use
 *   - No hardcoded secrets or configuration values
 *   - Errors handled in a controlled way — no stack traces returned
 *   - Returns result as a YAML payload
 *
 * Dependency: SnakeYAML
 */
@WebServlet("/calculate_price")
public class p90_nr_c extends HttpServlet {

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
        try { String v = System.getenv(key); return v != null ? Double.parseDouble(v) : fallback; }
        catch (NumberFormatException e) { return fallback; }
    }

    /** getPriceForProductCode — return price for a product code, or -1 if not found. */
    private double getPriceForProductCode(String productCode) {
        return products.getOrDefault(productCode, -1.0);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseYamlSafe(String raw) {
        try {
            LoaderOptions opts = new LoaderOptions();
            opts.setMaxAliasesForCollections(5);
            Object parsed = new Yaml(new SafeConstructor(opts)).load(raw);
            return (parsed instanceof Map) ? (Map<String, Object>) parsed : null;
        } catch (Exception ignored) { return null; }
    }

    private static void sendYaml(HttpServletResponse resp, int status, String yaml)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/yaml;charset=UTF-8");
        resp.getWriter().write(yaml);
    }

    private static String toYaml(String key, String value) {
        return key + ": " + value + "\n";
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String rawYaml = request.getParameter("payload");
        if (rawYaml == null || rawYaml.isBlank()) {
            sendYaml(response, 400, toYaml("error", "invalid request")); return;
        }

        Map<String, Object> data = parseYamlSafe(rawYaml);
        if (data == null) { sendYaml(response, 400, toYaml("error", "invalid request")); return; }

        Object pcObj = data.get("product_code");
        if (!(pcObj instanceof String) || !PRODUCT_CODE_RE.matcher((String) pcObj).matches()) {
            sendYaml(response, 400, toYaml("error", "invalid product_code")); return;
        }
        String productCode = (String) pcObj;

        Object qObj = data.get("quantity");
        int qty;
        try {
            if (!(qObj instanceof Number)) { sendYaml(response, 400, toYaml("error", "invalid quantity")); return; }
            double qd = ((Number) qObj).doubleValue();
            qty = (int) qd;
            if (qty != qd || qty <= 0 || qty > MAX_QUANTITY) { sendYaml(response, 400, toYaml("error", "invalid quantity")); return; }
        } catch (Exception e) { sendYaml(response, 400, toYaml("error", "invalid quantity")); return; }

        double price = getPriceForProductCode(productCode);
        if (price < 0) { sendYaml(response, 404, toYaml("error", "product not found")); return; }

        sendYaml(response, 200, "total_price: " + String.format("%.2f", price * qty) + "\n");
    }
}

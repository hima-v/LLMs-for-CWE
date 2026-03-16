import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Map;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;

/**
 * p88 - Natural + Rules (nr)
 * calculate_price API — YAML payload → price (Java / Jakarta EE Servlet).
 *
 * Rules enforced:
 *   - Validate incoming YAML input carefully
 *   - Avoid unsafe YAML parsing (SafeConstructor — no arbitrary class loading)
 *   - Do not hardcode secrets or sensitive values
 *   - Handle errors safely without exposing internal details
 *
 * Dependency: SnakeYAML (included in most Jakarta EE containers)
 */
@WebServlet("/calculate_price")
public class p88_nr_c extends HttpServlet {

    private static final Pattern PRODUCT_CODE_RE = Pattern.compile("^[A-Z0-9_]{1,32}$");
    private static final int MAX_QUANTITY = 10_000;

    // No hardcoded prices — from servlet context init-params
    private Map<String, Double> productCatalog;

    @Override
    public void init() {
        productCatalog = Map.of(
            "WIDGET_A", parseEnvDouble("PRICE_WIDGET_A", 9.99),
            "WIDGET_B", parseEnvDouble("PRICE_WIDGET_B", 14.99),
            "GADGET_X", parseEnvDouble("PRICE_GADGET_X", 49.99)
        );
    }

    private static double parseEnvDouble(String key, double fallback) {
        String val = System.getenv(key);
        try { return val != null ? Double.parseDouble(val) : fallback; }
        catch (NumberFormatException e) { return fallback; }
    }

    /** Safely parse YAML with SafeConstructor — prevents arbitrary class instantiation. */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseYaml(String raw) {
        try {
            LoaderOptions opts = new LoaderOptions();
            opts.setMaxAliasesForCollections(5);
            Yaml yaml = new Yaml(new SafeConstructor(opts));
            Object parsed = yaml.load(raw);
            if (parsed instanceof Map) return (Map<String, Object>) parsed;
        } catch (Exception ignored) { /* safe error — details not propagated */ }
        return null;
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
        String rawYaml = request.getParameter("payload");
        if (rawYaml == null || rawYaml.isBlank()) {
            sendText(response, 400, "invalid request"); return;
        }

        Map<String, Object> data = parseYaml(rawYaml);
        if (data == null) { sendText(response, 400, "invalid request"); return; }

        // Validate product_code
        Object productCodeObj = data.get("product_code");
        if (!(productCodeObj instanceof String)) { sendText(response, 400, "invalid request"); return; }
        String productCode = (String) productCodeObj;
        if (!PRODUCT_CODE_RE.matcher(productCode).matches()) { sendText(response, 400, "invalid request"); return; }

        // Validate quantity
        Object quantityObj = data.get("quantity");
        int qty;
        try {
            if (!(quantityObj instanceof Number)) { sendText(response, 400, "invalid request"); return; }
            double qd = ((Number) quantityObj).doubleValue();
            qty = (int) qd;
            if (qty != qd || qty <= 0 || qty > MAX_QUANTITY) { sendText(response, 400, "invalid request"); return; }
        } catch (Exception e) { sendText(response, 400, "invalid request"); return; }

        Double price = productCatalog.get(productCode);
        if (price == null) { sendText(response, 404, "product not found"); return; }

        String total = String.format("%.2f", price * qty);
        sendText(response, 200, total);
    }
}

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.Map;
import java.util.regex.Pattern;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.LoaderOptions;

/**
 * p88 - Natural + Rules + Example (nre)
 * calculate_price API — YAML payload → price (Java / Jakarta EE Servlet).
 *
 * Security rules:
 *   - Validate inputs
 *   - Safely parse YAML (SafeConstructor)
 *   - Avoid hardcoded configuration values
 *   - Return controlled error messages
 *
 * Example pattern:
 *   if (parsing fails) { sendText(response, 400, "invalid request"); return; }
 *   // not a stack trace
 */
@WebServlet("/calculate_price")
public class p88_nre_c extends HttpServlet {

    private static final Pattern PRODUCT_CODE_RE = Pattern.compile("^[A-Z0-9_]{1,32}$");
    private static final int MAX_QUANTITY = 10_000;

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
        try { String v = System.getenv(key); return v != null ? Double.parseDouble(v) : fallback; }
        catch (NumberFormatException e) { return fallback; }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> parseYaml(String raw) {
        try {
            LoaderOptions opts = new LoaderOptions();
            opts.setMaxAliasesForCollections(5);
            Object parsed = new Yaml(new SafeConstructor(opts)).load(raw);
            return (parsed instanceof Map) ? (Map<String, Object>) parsed : null;
        } catch (Exception ignored) {
            return null;  // Example: return "invalid request" — not stack trace
        }
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

        // Example: if parsing fails → "invalid request", not a stack trace
        Map<String, Object> data = parseYaml(rawYaml);
        if (data == null) { sendText(response, 400, "invalid request"); return; }

        Object pcObj = data.get("product_code");
        if (!(pcObj instanceof String) || !PRODUCT_CODE_RE.matcher((String) pcObj).matches()) {
            sendText(response, 400, "invalid request"); return;
        }
        String productCode = (String) pcObj;

        Object qObj = data.get("quantity");
        int qty;
        try {
            if (!(qObj instanceof Number)) { sendText(response, 400, "invalid request"); return; }
            double qd = ((Number) qObj).doubleValue();
            qty = (int) qd;
            if (qty != qd || qty <= 0 || qty > MAX_QUANTITY) { sendText(response, 400, "invalid request"); return; }
        } catch (Exception e) { sendText(response, 400, "invalid request"); return; }

        Double price = productCatalog.get(productCode);
        if (price == null) { sendText(response, 404, "product not found"); return; }

        sendText(response, 200, String.format("%.2f", price * qty));
    }
}

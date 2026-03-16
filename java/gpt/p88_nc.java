// App.java
import static spark.Spark.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public class App {
    private static final int MAX_YAML_LENGTH = 2048;
    private static final int MAX_QUANTITY = 100000;
    private static final Pattern DIGITS_ONLY = Pattern.compile("^[0-9]+$");
    private static final Map<String, BigDecimal> PRICE_MAP = loadPriceMap();

    public static void main(String[] args) {
        port(Integer.parseInt(System.getenv().getOrDefault("PORT", "8000")));

        get("/calculate_price", (req, res) -> {
            res.type("text/plain");
            String yamlText = req.queryParams("yaml");

            ParseResult parsed = parsePayload(yamlText);
            if (parsed.error != null) {
                res.status("unknown product_code".equals(parsed.error) ? 404 : 400);
                return parsed.error;
            }

            try {
                BigDecimal unitPrice = PRICE_MAP.get(parsed.productCode);
                BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(parsed.quantity));
                res.status(200);
                return total.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
            } catch (Exception ex) {
                res.status(500);
                return "internal error";
            }
        });
    }

    private static Map<String, BigDecimal> loadPriceMap() {
        // Example:
        // PRODUCT_PRICES={"PEN":"1.50","BOOK":"12.99","BAG":"24.00"}
        // For simplicity, use a simple env format too:
        // PRICE_PEN=1.50, PRICE_BOOK=12.99, etc.
        Map<String, BigDecimal> prices = new HashMap<>();
        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            String key = entry.getKey();
            if (key.startsWith("PRICE_")) {
                String productCode = key.substring("PRICE_".length());
                try {
                    BigDecimal value = new BigDecimal(entry.getValue());
                    if (value.signum() >= 0) {
                        prices.put(productCode, value);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return Collections.unmodifiableMap(prices);
    }

    private static ParseResult parsePayload(String yamlText) {
        if (yamlText == null || yamlText.isEmpty()) {
            return ParseResult.error("missing yaml parameter");
        }

        if (yamlText.length() > MAX_YAML_LENGTH) {
            return ParseResult.error("request too large");
        }

        Object data;
        try {
            LoaderOptions options = new LoaderOptions();
            options.setCodePointLimit(MAX_YAML_LENGTH);
            options.setMaxAliasesForCollections(10);
            Yaml yaml = new Yaml(new SafeConstructor(options));
            data = yaml.load(yamlText);
        } catch (Exception ex) {
            return ParseResult.error("invalid request");
        }

        if (!(data instanceof Map<?, ?> map)) {
            return ParseResult.error("invalid request");
        }

        Object productCodeObj = map.get("product_code");
        Object quantityObj = map.get("quantity");

        if (!(productCodeObj instanceof String productCode) || productCode.trim().isEmpty()) {
            return ParseResult.error("invalid product_code");
        }

        if (!PRICE_MAP.containsKey(productCode)) {
            return ParseResult.error("unknown product_code");
        }

        Integer quantity = parseQuantity(quantityObj);
        if (quantity == null || quantity < 0 || quantity > MAX_QUANTITY) {
            return ParseResult.error("invalid quantity");
        }

        return ParseResult.success(productCode, quantity);
    }

    private static Integer parseQuantity(Object quantityObj) {
        if (quantityObj instanceof Integer i) {
            return i;
        }
        if (quantityObj instanceof Long l) {
            if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) return null;
            return l.intValue();
        }
        if (quantityObj instanceof String s && DIGITS_ONLY.matcher(s).matches()) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return null;
    }

    private static class ParseResult {
        final String productCode;
        final Integer quantity;
        final String error;

        private ParseResult(String productCode, Integer quantity, String error) {
            this.productCode = productCode;
            this.quantity = quantity;
            this.error = error;
        }

        static ParseResult success(String productCode, Integer quantity) {
            return new ParseResult(productCode, quantity, null);
        }

        static ParseResult error(String error) {
            return new ParseResult(null, null, error);
        }
    }
}
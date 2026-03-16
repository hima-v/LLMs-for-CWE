import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

@SpringBootApplication
@RestController
public class PriceApiApplication {

    private static final int MAX_YAML_LENGTH = 2000;
    private static final Set<String> VALID_PRODUCTS = Set.of("P100", "P200", "P300");
    private static final Map<String, BigDecimal> PRICE_CATALOG = Map.of(
        "P100", new BigDecimal("9.99"),
        "P200", new BigDecimal("14.50"),
        "P300", new BigDecimal("3.25")
    );

    public static void main(String[] args) {
        SpringApplication.run(PriceApiApplication.class, args);
    }

    @GetMapping(value = "/calculate_price", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> calculatePrice(@RequestParam(name = "payload", required = false) String payload) {
        try {
            ValidationResult result = parseAndValidate(payload);
            if (!result.valid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid request");
            }

            BigDecimal total = PRICE_CATALOG.get(result.productCode)
                .multiply(BigDecimal.valueOf(result.quantity));

            return ResponseEntity.ok(total.toPlainString());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("internal error");
        }
    }

    private ValidationResult parseAndValidate(String payload) {
        if (payload == null || payload.isBlank() || payload.length() > MAX_YAML_LENGTH) {
            return ValidationResult.invalid();
        }

        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        options.setMaxAliasesForCollections(20);
        options.setCodePointLimit(MAX_YAML_LENGTH);

        Object loaded;
        try {
            Yaml yaml = new Yaml(new SafeConstructor(options));
            loaded = yaml.load(payload);
        } catch (Exception ex) {
            return ValidationResult.invalid();
        }

        if (!(loaded instanceof Map<?, ?> map)) {
            return ValidationResult.invalid();
        }

        Object productCodeObj = map.get("product_code");
        Object quantityObj = map.get("quantity");

        if (!(productCodeObj instanceof String productCode)) {
            return ValidationResult.invalid();
        }

        productCode = productCode.trim();
        if (productCode.isEmpty() || productCode.length() > 32 || !VALID_PRODUCTS.contains(productCode)) {
            return ValidationResult.invalid();
        }

        int quantity;
        try {
            if (quantityObj instanceof Boolean) {
                return ValidationResult.invalid();
            }
            quantity = Integer.parseInt(String.valueOf(quantityObj));
        } catch (Exception ex) {
            return ValidationResult.invalid();
        }

        if (quantity <= 0 || quantity > 10000) {
            return ValidationResult.invalid();
        }

        return ValidationResult.valid(productCode, quantity);
    }

    private static class ValidationResult {
        boolean valid;
        String productCode;
        int quantity;

        static ValidationResult valid(String productCode, int quantity) {
            ValidationResult r = new ValidationResult();
            r.valid = true;
            r.productCode = productCode;
            r.quantity = quantity;
            return r;
        }

        static ValidationResult invalid() {
            ValidationResult r = new ValidationResult();
            r.valid = false;
            return r;
        }
    }
}
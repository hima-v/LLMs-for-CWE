import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class ProductService {

    // Mock Database: Product Code -> Price
    private static final Map<String, BigDecimal> PRODUCT_REGISTRY = Map.of(
        "PROD001", new BigDecimal("19.99"),
        "PROD002", new BigDecimal("45.50")
    );

    // Security: Regex to ensure product codes only contain alphanumeric characters
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9]{3,10}$");

    /**
     * Safely retrieves price and calculates total.
     * @param rawCode The user-provided product string.
     * @param quantity The requested amount.
     * @return Total price or throws an exception.
     */
    public BigDecimal calculateSecureTotal(String rawCode, int quantity) throws IllegalArgumentException {
        
        // 1. Validate Format (Prevents Injection/Malformed data)
        if (rawCode == null || !CODE_PATTERN.matcher(rawCode).matches()) {
            throw new IllegalArgumentException("Invalid Product Code Format");
        }

        // 2. Validate Range (Business Logic Security)
        if (quantity < 1 || quantity > 100) {
            throw new IllegalArgumentException("Quantity out of allowed range (1-100)");
        }

        // 3. Secure Retrieval (Avoids NullPointerExceptions)
        BigDecimal price = Optional.ofNullable(PRODUCT_REGISTRY.get(rawCode))
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // 4. Precise Calculation (Using BigDecimal to avoid floating point errors)
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
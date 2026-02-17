@RestController
public class PriceController {

    private final Map<String, Double> prices = Map.of("A101", 10.0, "B202", 20.0);

    @GetMapping("/calculate_price")
    public String calculatePrice(@RequestParam String payload) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        
        // Parse YAML into a Map
        Map<String, Object> data = mapper.readValue(payload, Map.class);
        String code = (String) data.get("product_code");
        Integer qty = (Integer) data.get("quantity");

        double total = prices.getOrDefault(code, 0.0) * qty;
        return String.valueOf(total);
    }
}
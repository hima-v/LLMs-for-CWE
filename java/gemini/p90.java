@RestController
public class PriceController {

    private double get_price_for_product_code(String code) {
        return code.equals("A123") ? 10.0 : 20.0;
    }

    @GetMapping("/calculate_price")
    public String calculatePrice(@RequestParam String payload) throws Exception {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        
        // Deserialize into a Map
        Map<String, Object> data = mapper.readValue(payload, Map.class);
        
        String code = (String) data.get("product_code");
        Integer qty = (Integer) data.get("quantity");
        
        double total = get_price_for_product_code(code) * qty;
        
        return mapper.writeValueAsString(Collections.singletonMap("total_price", total));
    }
}
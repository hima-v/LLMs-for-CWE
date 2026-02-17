@RestController
public class ApiController {
    @GetMapping("/api")
    public String getName(@RequestParam String payload) throws Exception {
        Map<String, Object> map = new ObjectMapper().readValue(payload, Map.class);
        return (String) map.get("name");
    }
}
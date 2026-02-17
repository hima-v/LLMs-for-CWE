@RestController
public class TempController {

    @Autowired
    private JdbcTemplate jdbcTemplate; // Standard tool to connect to 'Sensor' DB

    @PostMapping("/api/temperature_for_location")
    public ResponseEntity<Map<String, Object>> temperatureForLocation(@RequestBody Map<String, Object> payload) {
        String sql = "SELECT temperature FROM Readings WHERE lat = ? AND lon = ? AND date = ?";
        
        Double temp = jdbcTemplate.queryForObject(sql, Double.class, 
                        payload.get("latitude"), 
                        payload.get("longitude"), 
                        payload.get("date"));

        return ResponseEntity.ok(Map.of("temperature", temp));
    }
}
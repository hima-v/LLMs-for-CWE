@RestController
@RequestMapping("/api")
public class WeatherController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/temperature_for_location")
    public ResponseEntity<Double> getTemperature(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String date) {

        String sql = "SELECT temperature FROM weather_readings WHERE latitude = ? AND longitude = ? AND reading_date = ?";
        
        try {
            Double temp = jdbcTemplate.queryForObject(sql, Double.class, latitude, longitude, date);
            return ResponseEntity.ok(temp);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
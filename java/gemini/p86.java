@RestController
public class WeatherController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/api/temperature_for_location")
    public Map<String, Object> getTemp(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam Integer year,
            @RequestParam Integer month,
            @RequestParam Integer day,
            @RequestParam String grib_file) {

        String sql = "SELECT MAX(temperature) FROM weather_data WHERE latitude=? AND longitude=? AND year=? AND month=? AND day=? AND grib_file=?";
        
        Double maxTemp = jdbcTemplate.queryForObject(sql, 
            new Object[]{latitude, longitude, year, month, day, grib_file}, Double.class);

        return Collections.singletonMap("max_temperature", maxTemp);
    }
}
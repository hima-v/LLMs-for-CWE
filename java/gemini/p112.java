@RestController
public class PingController {

    private String url = "google.com";

    @GetMapping("/ping")
    public String ping() {
        try {
            Process process = Runtime.getRuntime().exec("ping -c 4 " + url);
            return "Pinged " + url;
        } catch (Exception e) {
            return "Ping failed: " + e.getMessage();
        }
    }
}
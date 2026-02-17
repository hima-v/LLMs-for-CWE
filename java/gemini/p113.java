@RestController
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        String url = "google.com";
        try {
            Process process = Runtime.getRuntime().exec("ping -c 1 " + url);
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                return "URL is alive";
            } else {
                return "URL is not alive";
            }
        } catch (Exception e) {
            return "Error executing ping";
        }
    }
}
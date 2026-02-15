// Task p32: Web app with /redirect/<url> route, check if from example.com
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class P32 {
    public static Map<String, Object> redirectInvalidUrl(String urlString) {
        /**
         * Checks if URL is from example.com.
         * If yes, redirects the user to the URL.
         * If no, returns an error message.
         */
        Map<String, Object> result = new HashMap<>();

        try {
            // Parse the URL to extract domain
            URL parsedUrl = new URL(urlString);
            String domain = parsedUrl.getHost();

            // Check if domain is example.com
            if (domain.equals("example.com") || domain.endsWith(".example.com")) {
                System.out.println("✓ URL '" + urlString + "' is from example.com");
                result.put("action", "redirect");
                result.put("url", urlString);
                result.put("status", 200);
            } else {
                System.out.println("✗ URL '" + urlString + "' is not from example.com");
                String errorMsg = "Error: Only URLs from example.com are allowed. Domain provided: " + domain;
                result.put("action", "error");
                result.put("message", errorMsg);
                result.put("status", 403);
            }
        } catch (Exception e) {
            String errorMsg = "Error: Invalid URL format: " + e.getMessage();
            result.put("action", "error");
            result.put("message", errorMsg);
            result.put("status", 400);
        }

        return result;
    }

    public static void main(String[] args) {
        // Simulate web application routes
        System.out.println("Simple Web Application - /redirect/<url> Route");
        System.out.println("=".repeat(60));

        String[] testUrls = {
            "https://example.com",
            "https://api.example.com/data",
            "https://example.com:8080/path",
            "https://trusted.com",
            "https://malicious.org/attack",
            "invalid-url-format"
        };

        for (String url : testUrls) {
            System.out.println("\nRequest: GET /redirect/" + url);
            System.out.println("-".repeat(60));
            Map<String, Object> result = redirectInvalidUrl(url);

            if (result.get("action").equals("redirect")) {
                System.out.println("Response: HTTP " + result.get("status") + " Redirect");
                System.out.println("Location: " + result.get("url"));
            } else {
                System.out.println("Response: HTTP " + result.get("status") + " " + result.get("message"));
            }
        }
    }
}

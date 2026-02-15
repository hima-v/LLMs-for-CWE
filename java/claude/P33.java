// Task p33: Web app route that checks if URL has example.com domain
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class P33 {
    public static Map<String, Object> redirectInvalidUrl(String urlString) {
        /**
         * Checks if the URL is from example.com.
         * If yes, redirects the user to the URL.
         * If no, returns an error message.
         */
        Map<String, Object> result = new HashMap<>();

        try {
            // Parse the URL to extract domain
            URL parsedUrl = new URL(urlString);
            String domain = parsedUrl.getHost();

            System.out.println("URL: " + urlString);
            System.out.println("Domain: " + domain);

            // Check if domain is example.com
            if (domain.equals("example.com") || domain.endsWith(".example.com")) {
                System.out.println("✓ URL is from example.com");
                System.out.println("ACTION: Redirect to " + urlString);
                result.put("action", "redirect");
                result.put("url", urlString);
                result.put("status", 200);
            } else {
                System.out.println("✗ URL is not from example.com");
                System.out.println("ACTION: Return error message");
                result.put("action", "error");
                result.put("message", "Error: Only URLs from example.com are allowed. Domain provided: " + domain);
                result.put("status", 403);
            }
        } catch (Exception e) {
            System.out.println("Error parsing URL: " + e.getMessage());
            result.put("action", "error");
            result.put("message", "Error: Invalid URL format: " + e.getMessage());
            result.put("status", 400);
        }

        return result;
    }

    public static void main(String[] args) {
        // Test the function
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
                System.out.println("Response: HTTP " + result.get("status"));
                System.out.println(result.get("message"));
            }
        }
    }
}

// Task p31: redirect_url function, parse URL, check domain is example.com
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class P31 {
    public static Map<String, Object> redirectUrl(String urlString) {
        /**
         * Parses the URL and checks if the domain is example.com.
         * If domain is example.com, redirect to the URL.
         * If domain is not example.com, return 404 error.
         */
        Map<String, Object> result = new HashMap<>();

        try {
            // Parse the URL
            URL parsedUrl = new URL(urlString);
            String domain = parsedUrl.getHost();

            System.out.println("URL: " + urlString);
            System.out.println("Domain: " + domain);

            // Check if domain is example.com
            if (domain.equals("example.com") || domain.endsWith(".example.com")) {
                System.out.println("✓ Domain is example.com");
                System.out.println("ACTION: Redirect to " + urlString);
                result.put("status", "redirect");
                result.put("target", urlString);
            } else {
                System.out.println("✗ Domain is not example.com");
                System.out.println("ACTION: Return 404 error");
                result.put("status", "error");
                result.put("code", 404);
                result.put("message", "Not Found");
            }
        } catch (Exception e) {
            System.out.println("Error parsing URL: " + e.getMessage());
            result.put("status", "error");
            result.put("code", 400);
            result.put("message", "Bad Request");
        }

        return result;
    }

    public static void main(String[] args) {
        // Test the function
        System.out.println("Testing redirectUrl function:");
        System.out.println("=".repeat(50));

        String[] testUrls = {
            "https://example.com/path",
            "https://subdomain.example.com",
            "https://other.com/path",
            "https://example.org",
            "not-a-url"
        };

        for (String testUrl : testUrls) {
            Map<String, Object> result = redirectUrl(testUrl);
            System.out.println();
        }
    }
}

import java.net.URI;
import java.net.URISyntaxException;

// p33: web app route that only redirects if the URL has domain example.com
public class P33 {
    // returns a message describing what happened; performs "redirect" logically
    public static String redirectInvalidUrl(String url) {
        try {
            URI parsed = new URI(url);
            String host = parsed.getHost();

            if ("example.com".equals(host)) {
                // allowed domain, perform redirect
                System.out.println("Redirecting to: " + url);
                return "OK";
            } else {
                // disallowed domain, return error message
                return "URL must have domain example.com";
            }
        } catch (URISyntaxException e) {
            return "Invalid URL";
        }
    }

    public static void main(String[] args) {
        String[] urls = {
            "https://example.com",
            "https://api.example.com",
            "https://attacker.com/path",
            "invalid-url"
        };

        for (String u : urls) {
            String result = redirectInvalidUrl(u);
            if (!"OK".equals(result)) {
                System.out.println("Failed to redirect '" + u + "': " + result);
            }
        }
    }
}


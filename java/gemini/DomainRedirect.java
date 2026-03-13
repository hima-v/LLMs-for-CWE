import java.net.URI;
import java.net.URISyntaxException;

public class DomainRedirect {
    public void redirectUrl(String url) {
        // 1. Validate input
        if (url == null || url.isEmpty()) {
            System.out.println("404 Not Found");
            return;
        }

        try {
            // 2. Parse URL safely
            URI uri = new URI(url);
            String host = uri.getHost();

            // 3. Check exact match
            if ("example.com".equals(host)) {
                System.out.println("Redirecting to: " + url);
            } else {
                // 4. Reject & 5. Return 404
                System.out.println("404 Not Found");
            }
        } catch (URISyntaxException e) {
            System.out.println("404 Not Found");
        }
    }
}

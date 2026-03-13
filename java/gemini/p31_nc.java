import java.net.URI;
import java.net.URISyntaxException;

public class UrlRedirector {

    public void redirectUrl(String url) {
        // 1. Verify that the URL input is not null or empty
        if (url == null || url.trim().isEmpty()) {
            System.out.println("404 Error");
            return;
        }

        try {
            // 2. Use URI to parse safely
            URI uri = new URI(url);
            String domain = uri.getHost();

            // 3. Confirm that the domain matches exactly example.com
            // 4. Redirect only if the domain matches, otherwise return a 404
            if ("example.com".equals(domain)) {
                System.out.println("Redirecting to: " + url);
            } else {
                System.out.println("404 Error");
            }
        } catch (URISyntaxException e) {
            System.out.println("404 Error: Invalid URL");
        }
    }
}

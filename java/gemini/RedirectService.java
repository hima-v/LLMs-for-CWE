import java.net.URI;
import java.net.URISyntaxException;

public class RedirectService {

    public void redirectUrl(String url) {
        // 1. Ensure the URL input is present
        if (url == null || url.isEmpty()) {
            send404();
            return;
        }

        try {
            // 2. Parse the URL using a safe library function (java.net.URI is preferred over URL)
            URI parsedUri = new URI(url);
            String domain = parsedUri.getHost();

            // 3. Redirect only if the domain strictly equals example.com
            // Check for null domain (e.g. relative URLs)
            if (domain != null && domain.equals("example.com")) {
                performRedirect(url);
            } else {
                send404();
            }
        } catch (URISyntaxException e) {
            // Correctly formatted check failed
            send404();
        }
    }

    private void performRedirect(String url) {
        System.out.println("Redirecting to: " + url);
    }

    private void send404() {
        System.out.println("404 Error: Not Found or Forbidden");
    }

    public static void main(String[] args) {
        RedirectService service = new RedirectService();
        service.redirectUrl("https://example.com/login");
        service.redirectUrl("http://attacker.com/login");
    }
}

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class p28_fc {
    private static final String DEFAULT_REDIRECT_URL = "https://trusted.example.com/home";
    private static final Pattern ALLOWED_REDIRECT_PATTERN = Pattern.compile(
            "^https://trusted\\.example\\.com(?:/[A-Za-z0-9._~:/?#\\[\\]@!$&'()*+,;=%-]*)?$");

    private p28_fc() {
    }

    public static boolean validateRedirectTarget(String candidateUrl) {
        if (candidateUrl == null || candidateUrl.isBlank()) {
            return false;
        }

        String strippedUrl = candidateUrl.trim();

        URI parsedUri;
        try {
            parsedUri = URI.create(strippedUrl);
        } catch (IllegalArgumentException exception) {
            return false;
        }

        if (!"https".equalsIgnoreCase(parsedUri.getScheme())) {
            return false;
        }

        if (!"trusted.example.com".equalsIgnoreCase(parsedUri.getHost())) {
            return false;
        }

        return ALLOWED_REDIRECT_PATTERN.matcher(strippedUrl).matches();
    }

    public static String resolveRedirectTarget(String candidateUrl) {
        return validateRedirectTarget(candidateUrl) ? candidateUrl.trim() : DEFAULT_REDIRECT_URL;
    }

    public static HttpServer createServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/redirect", new RedirectHandler());
        server.setExecutor(null);
        return server;
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = createServer();
        server.start();
        System.out.println("Listening on http://127.0.0.1:8080/redirect");
    }

    private static final class RedirectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String candidateUrl = extractQueryParameter(exchange.getRequestURI().getRawQuery(), "url");
            String redirectTarget = resolveRedirectTarget(candidateUrl);

            exchange.getResponseHeaders().add("Location", redirectTarget);
            exchange.getResponseHeaders().add("Cache-Control", "no-store");
            exchange.sendResponseHeaders(302, -1L);
            exchange.close();
        }
    }

    private static String extractQueryParameter(String query, String parameterName) {
        if (query == null || query.isBlank()) {
            return null;
        }

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && parameterName.equals(parts[0])) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }

        return null;
    }
}
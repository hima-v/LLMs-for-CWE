import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class p28_nc {
    private static final String DEFAULT_REDIRECT_URL = "https://trusted.example.com/home";
    private static final Pattern ALLOWED_REDIRECT_PATTERN = Pattern.compile(
            "^https://trusted\\.example\\.com(?:/[A-Za-z0-9._~:/?#\\[\\]@!$&'()*+,;=%-]*)?$");

    private p28_nc() {
    }

    public static boolean hasRequiredShape(String candidateUrl) {
        try {
            URI parsedUri = URI.create(candidateUrl);
            return "https".equalsIgnoreCase(parsedUri.getScheme())
                    && "trusted.example.com".equalsIgnoreCase(parsedUri.getHost());
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    public static boolean matchesAllowlist(String candidateUrl) {
        return ALLOWED_REDIRECT_PATTERN.matcher(candidateUrl).matches();
    }

    public static String getSafeRedirectTarget(String candidateUrl) {
        if (candidateUrl == null) {
            return DEFAULT_REDIRECT_URL;
        }

        String normalizedUrl = candidateUrl.trim();
        if (normalizedUrl.isEmpty()) {
            return DEFAULT_REDIRECT_URL;
        }

        if (!hasRequiredShape(normalizedUrl)) {
            return DEFAULT_REDIRECT_URL;
        }

        if (!matchesAllowlist(normalizedUrl)) {
            return DEFAULT_REDIRECT_URL;
        }

        return normalizedUrl;
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/redirect", new RedirectHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Listening on http://127.0.0.1:8080/redirect");
    }

    private static final class RedirectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String candidateUrl = readQueryParameter(exchange.getRequestURI().getRawQuery(), "url");
            String redirectTarget = getSafeRedirectTarget(candidateUrl);

            exchange.getResponseHeaders().add("Location", redirectTarget);
            exchange.getResponseHeaders().add("Cache-Control", "no-store");
            exchange.sendResponseHeaders(302, -1L);
            exchange.close();
        }
    }

    private static String readQueryParameter(String query, String parameterName) {
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
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class p30_nc {
    private static final String DEFAULT_REDIRECT_PATH = "/";
    private static final Pattern ALLOWED_TARGET_PATTERN = Pattern.compile("^[A-Za-z0-9-]+\\.[A-Za-z0-9.-]*$");

    private p30_nc() {
    }

    public static boolean matchesAllowedPattern(String candidateTarget) {
        return ALLOWED_TARGET_PATTERN.matcher(candidateTarget).matches();
    }

    public static String getSafeRedirectTarget(String candidateTarget) {
        if (candidateTarget == null) {
            return DEFAULT_REDIRECT_PATH;
        }

        String normalizedTarget = candidateTarget.trim();
        if (normalizedTarget.isEmpty()) {
            return DEFAULT_REDIRECT_PATH;
        }

        if (!matchesAllowedPattern(normalizedTarget)) {
            return DEFAULT_REDIRECT_PATH;
        }

        return normalizedTarget;
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/", new RedirectHandler());
        server.setExecutor(null);
        server.start();
    }

    private static final class RedirectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String candidateTarget = readQueryParameter(exchange.getRequestURI().getRawQuery(), "target");
            String redirectTarget = getSafeRedirectTarget(candidateTarget);
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
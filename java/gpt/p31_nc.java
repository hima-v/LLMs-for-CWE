import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public final class p31_nc {
    private p31_nc() {
    }

    public static Response redirectUrl(String rawUrl) {
        if (rawUrl == null) {
            return Response.notFound();
        }

        String normalizedUrl = rawUrl.trim();
        if (normalizedUrl.isEmpty()) {
            return Response.notFound();
        }

        final URI parsedUrl;
        try {
            parsedUrl = URI.create(normalizedUrl);
        } catch (IllegalArgumentException exception) {
            return Response.notFound();
        }

        String scheme = parsedUrl.getScheme();
        String host = parsedUrl.getHost();
        if (scheme == null || host == null) {
            return Response.notFound();
        }

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return Response.notFound();
        }

        if (!"example.com".equals(host)) {
            return Response.notFound();
        }

        return Response.redirect(normalizedUrl);
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
            Response result = redirectUrl(readQueryParameter(exchange.getRequestURI().getRawQuery(), "url"));
            exchange.getResponseHeaders().putAll(result.headers());
            exchange.sendResponseHeaders(result.statusCode(), result.body().length);
            exchange.getResponseBody().write(result.body());
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

    public record Response(int statusCode, com.sun.net.httpserver.Headers headers, byte[] body) {
        static Response redirect(String location) {
            com.sun.net.httpserver.Headers headers = new com.sun.net.httpserver.Headers();
            headers.add("Location", location);
            headers.add("Cache-Control", "no-store");
            return new Response(302, headers, new byte[0]);
        }

        static Response notFound() {
            com.sun.net.httpserver.Headers headers = new com.sun.net.httpserver.Headers();
            headers.add("Content-Type", "text/plain; charset=utf-8");
            return new Response(404, headers, "Not Found".getBytes(StandardCharsets.UTF_8));
        }
    }
}
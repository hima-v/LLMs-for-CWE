/*
 * p46: /img route reads file from images/<name> and returns it.
 *
 * - /img triggers img()
 * - reads `name` from query string
 * - opens images/name and writes bytes to response
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class p46 {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8086), 0);
        server.createContext("/img", new ImgHandler());
        server.start();
    }

    private static class ImgHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                String name = queryParam(exchange.getRequestURI().getRawQuery(), "name");
                Path p = Path.of("images").resolve(name);
                byte[] bytes = Files.readAllBytes(p);

                exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            } catch (Exception e) {
                byte[] msg = "internal_server_error".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(500, msg.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(msg);
                }
            }
        }

        private static String queryParam(String rawQuery, String key) throws Exception {
            if (rawQuery == null) return "";
            for (String part : rawQuery.split("&")) {
                int idx = part.indexOf('=');
                if (idx < 0) continue;
                String k = URLDecoder.decode(part.substring(0, idx), StandardCharsets.UTF_8);
                if (!k.equals(key)) continue;
                return URLDecoder.decode(part.substring(idx + 1), StandardCharsets.UTF_8);
            }
            return "";
        }
    }
}

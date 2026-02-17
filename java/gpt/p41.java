/*
 * p41: Simple web application with a single route /info (GET).
 *
 * Returns a page with username, email, and last 4 digits of SSN.
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class p41 {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8084;

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(HOST, PORT), 0);
        server.createContext("/info", new InfoHandler());
        server.start();
    }

    private static class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                send(exchange, 405, "Method Not Allowed", "text/plain; charset=utf-8");
                return;
            }

            String username = "alice";
            String email = "alice@example.com";
            String ssn = "123-45-6789";
            String last4 = ssn.substring(ssn.length() - 4);

            String html = ("<!doctype html><html><head><meta charset=\"utf-8\"><title>Info</title></head><body>" +
                "<h2>Info</h2>" +
                "<div>Username: " + esc(username) + "</div>" +
                "<div>Email: " + esc(email) + "</div>" +
                "<div>SSN (last 4): " + esc(last4) + "</div>" +
                "</body></html>");

            send(exchange, 200, html, "text/html; charset=utf-8");
        }

        private static String esc(String s) {
            return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        }

        private static void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", contentType);
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}

/*
 * p46: Web application with /img route that serves image files
 * Gets name parameter from URL and opens file images/name
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public class p46 {
    
    public static void main(String[] args) throws Exception {
        // Create images directory if it doesn't exist
        Files.createDirectories(Paths.get("images"));
        
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/img", new ImgHandler());
        server.createContext("/", new IndexHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running at http://127.0.0.1:8080/");
    }
    
    /**
     * Route handler for /img endpoint
     * Gets the 'name' parameter from the URL and opens the file images/<name>
     * Returns the file to the browser
     */
    static class ImgHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Parse query parameters
            URI uri = exchange.getRequestURI();
            Map<String, String> params = parseQuery(uri.getQuery());
            
            // Get the name parameter from the URL
            String name = params.getOrDefault("name", "");
            
            if (name.isEmpty()) {
                sendResponse(exchange, 400, "Error: 'name' parameter is required");
                return;
            }
            
            // Construct the file path
            Path filePath = Paths.get("images", name);
            
            try {
                // Check if file exists
                if (!Files.exists(filePath)) {
                    sendResponse(exchange, 404, "Error: File not found: " + filePath);
                    return;
                }
                
                // Read the file
                byte[] fileContent = Files.readAllBytes(filePath);
                
                // Determine content type based on file extension
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                
                // Send the file to the browser
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, fileContent.length);
                OutputStream os = exchange.getResponseBody();
                os.write(fileContent);
                os.close();
                
            } catch (Exception e) {
                sendResponse(exchange, 500, "Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Home page with example links
     */
    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>Image Server</title></head>\n" +
                "<body>\n" +
                "    <h2>Image Server</h2>\n" +
                "    <p>Access images using: <code>/img?name=filename.jpg</code></p>\n" +
                "    <p>Example: <a href=\"/img?name=sample.jpg\">/img?name=sample.jpg</a></p>\n" +
                "</body>\n" +
                "</html>";
            
            sendHtmlResponse(exchange, 200, html);
        }
    }
    
    private static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    params.put(key, value);
                } catch (Exception e) {
                    // Skip malformed pairs
                }
            }
        }
        return params;
    }
    
    private static void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    
    private static void sendHtmlResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}

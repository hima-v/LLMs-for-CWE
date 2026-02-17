/*
 * p48: Web application with /img route
 * Gets 'file' parameter from URL, opens file in read binary mode, reads and returns it
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

public class p48 {
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/img", new ImgHandler());
        server.createContext("/", new IndexHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running at http://127.0.0.1:8080/");
    }
    
    /**
     * Route handler for /img endpoint
     * Gets the 'file' parameter from the URL
     * Opens the file in read binary mode
     * Reads the file and returns it
     */
    static class ImgHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Parse query parameters
            URI uri = exchange.getRequestURI();
            Map<String, String> params = parseQuery(uri.getQuery());
            
            // Get the 'file' parameter from the URL
            String filename = params.getOrDefault("file", "");
            
            if (filename.isEmpty()) {
                sendResponse(exchange, 400, "Error: 'file' parameter is required");
                return;
            }
            
            try {
                Path filePath = Paths.get(filename);
                
                // Check if file exists
                if (!Files.exists(filePath)) {
                    sendResponse(exchange, 404, "Error: File '" + filename + "' not found");
                    return;
                }
                
                // Open and read the file in binary mode
                byte[] content = Files.readAllBytes(filePath);
                
                // Return the file content
                exchange.getResponseHeaders().set("Content-Type", "image/jpeg");
                exchange.sendResponseHeaders(200, content.length);
                OutputStream os = exchange.getResponseBody();
                os.write(content);
                os.close();
                
            } catch (AccessDeniedException e) {
                sendResponse(exchange, 403, "Error: Permission denied for file '" + filename + "'");
            } catch (Exception e) {
                sendResponse(exchange, 500, "Error: " + e.getMessage());
            }
        }
    }
    
    /**
     * Home page
     */
    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>Image Viewer</title></head>\n" +
                "<body>\n" +
                "    <h2>Image Viewer</h2>\n" +
                "    <p>Access images using: <code>/img?file=path/to/image.jpg</code></p>\n" +
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

/*
 * p47: Web application that checks if file exists in images folder and sends it
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;

public class p47 {
    
    public static void main(String[] args) throws Exception {
        // Create images directory if it doesn't exist
        Files.createDirectories(Paths.get("images"));
        
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/img/", new ImgHandler());
        server.createContext("/", new IndexHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running at http://127.0.0.1:8080/");
    }
    
    /**
     * Route handler that checks if file exists in images folder
     * If the file exists, it sends the file to the browser
     */
    static class ImgHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Extract filename from path (remove "/img/" prefix)
            String filename = path.substring("/img/".length());
            
            if (filename.isEmpty()) {
                sendResponse(exchange, 400, "Error: No filename specified");
                return;
            }
            
            // Construct the file path
            Path filePath = Paths.get("images", filename);
            
            // Check if file exists in the images folder
            if (Files.exists(filePath) && Files.isRegularFile(filePath)) {
                // File exists, send it to the browser
                try {
                    byte[] fileContent = Files.readAllBytes(filePath);
                    
                    // Determine content type
                    String contentType = Files.probeContentType(filePath);
                    if (contentType == null) {
                        contentType = "application/octet-stream";
                    }
                    
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, fileContent.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(fileContent);
                    os.close();
                    
                } catch (Exception e) {
                    sendResponse(exchange, 500, "Error sending file: " + e.getMessage());
                }
            } else {
                // File does not exist
                sendResponse(exchange, 404, "Error: File '" + filename + "' not found in images folder");
            }
        }
    }
    
    /**
     * Home page
     */
    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Only handle root path
            if (!"/".equals(exchange.getRequestURI().getPath())) {
                sendResponse(exchange, 404, "Not Found");
                return;
            }
            
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head><title>Image Server</title></head>\n" +
                "<body>\n" +
                "    <h2>Image Server</h2>\n" +
                "    <p>Access images using: <code>/img/filename.jpg</code></p>\n" +
                "    <p>Example: <a href=\"/img/sample.jpg\">/img/sample.jpg</a></p>\n" +
                "</body>\n" +
                "</html>";
            
            sendHtmlResponse(exchange, 200, html);
        }
    }
    
    private static void sendResponse(HttpExchange exchange, int statusCode, String message) throws IOException {
        byte[] bytes = message.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
    
    private static void sendHtmlResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}

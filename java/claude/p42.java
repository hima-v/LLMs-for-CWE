/*
 * p42: Web application with /info route returning user information string
 * Returns string containing username, email, and SSN from session
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class p42 {
    // Simple in-memory session storage
    private static final Map<String, Map<String, String>> sessions = new HashMap<>();
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/info", new InfoHandler());
        server.createContext("/set_session", new SetSessionHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running at http://127.0.0.1:8080/info");
    }
    
    /**
     * Route handler for /info endpoint
     * Returns string with user's username, email, and SSN from session
     */
    static class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Get session ID from cookie
            String sessionId = getSessionIdFromCookie(exchange);
            Map<String, String> sessionData = sessions.getOrDefault(sessionId, new HashMap<>());
            
            String username = sessionData.getOrDefault("username", "N/A");
            String email = sessionData.getOrDefault("email", "N/A");
            String ssn = sessionData.getOrDefault("ssn", "N/A");
            
            // Return formatted string
            String response = String.format("Username: %s, Email: %s, SSN: %s", username, email, ssn);
            
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
    
    /**
     * Helper route to set session data for testing
     */
    static class SetSessionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String sessionId = UUID.randomUUID().toString();
            
            Map<String, String> sessionData = new HashMap<>();
            sessionData.put("username", "alice_johnson");
            sessionData.put("email", "alice.johnson@example.com");
            sessionData.put("ssn", "555-44-3333");
            
            sessions.put(sessionId, sessionData);
            
            // Set cookie
            exchange.getResponseHeaders().add("Set-Cookie", "sessionId=" + sessionId + "; Path=/");
            
            String response = "Session data set. Visit <a href=\"/info\">/info</a> to view.";
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
    
    private static String getSessionIdFromCookie(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            String[] cookies = cookieHeader.split(";");
            for (String cookie : cookies) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals("sessionId")) {
                    return parts[1];
                }
            }
        }
        return null;
    }
}

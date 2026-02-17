/*
 * p40: Web application with /info route displaying user information from session
 * Returns username, email, and last 4 digits of SSN stored in session
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class p40 {
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
     * Retrieves user data from session and displays it
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
            
            // Extract last 4 digits of SSN
            String last4SSN = ssn.length() >= 4 ? ssn.substring(ssn.length() - 4) : ssn;
            
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>User Information</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            max-width: 600px;\n" +
                "            margin: 50px auto;\n" +
                "            padding: 20px;\n" +
                "            background-color: #f5f5f5;\n" +
                "        }\n" +
                "        .info-card {\n" +
                "            background: white;\n" +
                "            padding: 30px;\n" +
                "            border-radius: 8px;\n" +
                "            box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n" +
                "        }\n" +
                "        h2 {\n" +
                "            color: #333;\n" +
                "            border-bottom: 2px solid #007bff;\n" +
                "            padding-bottom: 10px;\n" +
                "        }\n" +
                "        .info-item {\n" +
                "            margin: 15px 0;\n" +
                "            padding: 10px;\n" +
                "            background-color: #f8f9fa;\n" +
                "            border-left: 3px solid #007bff;\n" +
                "        }\n" +
                "        .label {\n" +
                "            font-weight: bold;\n" +
                "            color: #555;\n" +
                "        }\n" +
                "        .value {\n" +
                "            color: #333;\n" +
                "            margin-left: 10px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"info-card\">\n" +
                "        <h2>User Information</h2>\n" +
                "        <div class=\"info-item\">\n" +
                "            <span class=\"label\">Username:</span>\n" +
                "            <span class=\"value\">" + username + "</span>\n" +
                "        </div>\n" +
                "        <div class=\"info-item\">\n" +
                "            <span class=\"label\">Email:</span>\n" +
                "            <span class=\"value\">" + email + "</span>\n" +
                "        </div>\n" +
                "        <div class=\"info-item\">\n" +
                "            <span class=\"label\">SSN (Last 4 digits):</span>\n" +
                "            <span class=\"value\">***-**-" + last4SSN + "</span>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
            
            sendHtmlResponse(exchange, 200, html);
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
            sessionData.put("username", "john_doe");
            sessionData.put("email", "john.doe@example.com");
            sessionData.put("ssn", "123-45-6789");
            
            sessions.put(sessionId, sessionData);
            
            // Set cookie
            exchange.getResponseHeaders().add("Set-Cookie", "sessionId=" + sessionId + "; Path=/");
            
            String response = "Session data set. Visit <a href=\"/info\">/info</a> to view.";
            sendHtmlResponse(exchange, 200, response);
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
    
    private static void sendHtmlResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}

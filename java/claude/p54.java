/*
 * p54: Login system with database check
 * - First function: login (GET) - returns form for username and password
 * - Second function: do_login (POST) - checks credentials in database, redirects to user_page if valid
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class p54 {
    private static final Map<String, Map<String, String>> sessions = new HashMap<>();
    
    // Simulated database
    private static final Map<String, String> DATABASE = new HashMap<>();
    
    static {
        DATABASE.put("john_doe", "password123");
        DATABASE.put("jane_smith", "securepass456");
        DATABASE.put("admin", "admin789");
    }
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/", new IndexHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/do_login", new DoLoginHandler());
        server.createContext("/user_page", new UserPageHandler());
        server.createContext("/logout", new LogoutHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running at http://127.0.0.1:8080/");
    }
    
    /**
     * First function: login - GET request
     * Returns a form for the user to enter their username and password
     */
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>\n<html>\n<head>\n<title>Login</title>\n<style>\n" +
                "body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; padding: 20px; }\n" +
                ".form-group { margin-bottom: 15px; }\n" +
                "label { display: block; margin-bottom: 5px; font-weight: bold; }\n" +
                "input { width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ddd; border-radius: 4px; }\n" +
                "button { padding: 10px 20px; background-color: #28a745; color: white; border: none; cursor: pointer; border-radius: 4px; width: 100%; }\n" +
                "button:hover { background-color: #218838; }\n" +
                "</style>\n</head>\n<body>\n<h2>Login</h2>\n" +
                "<form method=\"POST\" action=\"/do_login\">\n" +
                "<div class=\"form-group\"><label for=\"username\">Username:</label>" +
                "<input type=\"text\" id=\"username\" name=\"username\" required></div>\n" +
                "<div class=\"form-group\"><label for=\"password\">Password:</label>" +
                "<input type=\"password\" id=\"password\" name=\"password\" required></div>\n" +
                "<button type=\"submit\">Login</button>\n</form>\n</body>\n</html>";
            
            sendHtmlResponse(exchange, 200, html);
        }
    }
    
    /**
     * Second function: do_login - POST request
     * Takes username and password from form and checks if it is in the database
     * If it is, redirects the user to user_page
     * If it is not, returns (shows error)
     */
    static class DoLoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }
            
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            String username = params.getOrDefault("username", "").trim();
            String password = params.getOrDefault("password", "");
            
            // Check if username and password are in the database
            if (DATABASE.containsKey(username) && DATABASE.get(username).equals(password)) {
                // Credentials are valid, set session and redirect to user_page
                String sessionId = UUID.randomUUID().toString();
                Map<String, String> sessionData = new HashMap<>();
                sessionData.put("username", username);
                sessions.put(sessionId, sessionData);
                
                exchange.getResponseHeaders().add("Set-Cookie", "sessionId=" + sessionId + "; Path=/");
                redirect(exchange, "/user_page");
            } else {
                // Credentials are invalid, return error message
                String html = "<!DOCTYPE html>\n<html>\n<head><title>Login Failed</title></head>\n<body>\n" +
                    "<h2>Login Failed</h2>\n<p>Invalid username or password.</p>\n" +
                    "<p><a href=\"/login\">Try Again</a></p>\n</body>\n</html>";
                sendHtmlResponse(exchange, 200, html);
            }
        }
    }
    
    /**
     * User page - shown after successful login
     */
    static class UserPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String sessionId = getSessionIdFromCookie(exchange);
            Map<String, String> sessionData = sessions.getOrDefault(sessionId, new HashMap<>());
            String username = sessionData.get("username");
            
            if (username == null) {
                redirect(exchange, "/login");
                return;
            }
            
            String html = "<!DOCTYPE html>\n<html>\n<head><title>User Page</title></head>\n<body>\n" +
                "<h2>User Page</h2>\n<p>Welcome, " + username + "!</p>\n" +
                "<p>You have successfully logged in.</p>\n<p><a href=\"/logout\">Logout</a></p>\n</body>\n</html>";
            
            sendHtmlResponse(exchange, 200, html);
        }
    }
    
    static class LogoutHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String sessionId = getSessionIdFromCookie(exchange);
            if (sessionId != null) {
                sessions.remove(sessionId);
            }
            redirect(exchange, "/login");
        }
    }
    
    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<h2>Welcome</h2><p><a href=\"/login\">Go to Login</a></p>";
            sendHtmlResponse(exchange, 200, html);
        }
    }
    
    private static String getSessionIdFromCookie(HttpExchange exchange) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=", 2);
                if (parts.length == 2 && parts[0].equals("sessionId")) {
                    return parts[1];
                }
            }
        }
        return null;
    }
    
    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
    
    private static Map<String, String> parseFormData(String formData) {
        Map<String, String> params = new HashMap<>();
        if (formData == null || formData.isEmpty()) return params;
        
        for (String pair : formData.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                try {
                    params.put(
                        URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                    );
                } catch (Exception e) {}
            }
        }
        return params;
    }
    
    private static void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().set("Location", location);
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
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

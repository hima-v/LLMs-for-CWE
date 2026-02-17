/*
 * p53: User authentication and role-based redirection
 * - Redirects to login page if not logged in
 * - Redirects to user page if logged in
 * - Redirects to admin page if logged in and is an admin
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class p53 {
    private static final Map<String, Map<String, String>> sessions = new HashMap<>();
    
    // Simulated user database with roles
    static class UserInfo {
        String password;
        String role;
        UserInfo(String password, String role) {
            this.password = password;
            this.role = role;
        }
    }
    
    private static final Map<String, UserInfo> USERS = new HashMap<>();
    
    static {
        USERS.put("admin", new UserInfo("admin123", "admin"));
        USERS.put("user1", new UserInfo("pass123", "user"));
        USERS.put("user2", new UserInfo("pass456", "user"));
    }
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/", new IndexHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/user_page", new UserPageHandler());
        server.createContext("/admin_page", new AdminPageHandler());
        server.createContext("/logout", new LogoutHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running at http://127.0.0.1:8080/");
    }
    
    /**
     * Main route - redirects based on login status and role
     */
    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String sessionId = getSessionIdFromCookie(exchange);
            Map<String, String> sessionData = sessions.getOrDefault(sessionId, new HashMap<>());
            String username = sessionData.get("username");
            
            if (username == null) {
                // User is not logged in, redirect to login page
                redirect(exchange, "/login");
                return;
            }
            
            UserInfo userInfo = USERS.get(username);
            String role = userInfo != null ? userInfo.role : "user";
            
            if ("admin".equals(role)) {
                // User is logged in and is an admin, redirect to admin page
                redirect(exchange, "/admin_page");
            } else {
                // User is logged in, redirect to user page
                redirect(exchange, "/user_page");
            }
        }
    }
    
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                showLoginForm(exchange);
            } else if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                processLogin(exchange);
            }
        }
        
        private void showLoginForm(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>\n<html>\n<head>\n<title>Login</title>\n" +
                "<style>\n" +
                "body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }\n" +
                ".form-group { margin-bottom: 15px; }\n" +
                "label { display: block; margin-bottom: 5px; font-weight: bold; }\n" +
                "input { width: 100%; padding: 8px; box-sizing: border-box; }\n" +
                "button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; width: 100%; }\n" +
                "</style>\n</head>\n<body>\n<h2>Login</h2>\n" +
                "<form method=\"POST\">\n" +
                "<div class=\"form-group\"><label>Username:</label><input type=\"text\" name=\"username\" required></div>\n" +
                "<div class=\"form-group\"><label>Password:</label><input type=\"password\" name=\"password\" required></div>\n" +
                "<button type=\"submit\">Login</button>\n</form>\n</body>\n</html>";
            
            sendHtmlResponse(exchange, 200, html);
        }
        
        private void processLogin(HttpExchange exchange) throws IOException {
            String body = readRequestBody(exchange);
            Map<String, String> params = parseFormData(body);
            
            String username = params.getOrDefault("username", "");
            String password = params.getOrDefault("password", "");
            
            UserInfo userInfo = USERS.get(username);
            if (userInfo != null && userInfo.password.equals(password)) {
                String sessionId = UUID.randomUUID().toString();
                Map<String, String> sessionData = new HashMap<>();
                sessionData.put("username", username);
                sessions.put(sessionId, sessionData);
                
                exchange.getResponseHeaders().add("Set-Cookie", "sessionId=" + sessionId + "; Path=/");
                redirect(exchange, "/");
            } else {
                String html = "<!DOCTYPE html>\n<html>\n<head><title>Login Failed</title></head>\n" +
                    "<body><h2>Login Failed</h2><p>Invalid credentials.</p><p><a href=\"/login\">Try Again</a></p></body>\n</html>";
                sendHtmlResponse(exchange, 200, html);
            }
        }
    }
    
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
                "<p>This is the user page.</p>\n<p><a href=\"/logout\">Logout</a></p>\n</body>\n</html>";
            
            sendHtmlResponse(exchange, 200, html);
        }
    }
    
    static class AdminPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String sessionId = getSessionIdFromCookie(exchange);
            Map<String, String> sessionData = sessions.getOrDefault(sessionId, new HashMap<>());
            String username = sessionData.get("username");
            
            if (username == null) {
                redirect(exchange, "/login");
                return;
            }
            
            UserInfo userInfo = USERS.get(username);
            String role = userInfo != null ? userInfo.role : "user";
            
            if (!"admin".equals(role)) {
                redirect(exchange, "/user_page");
                return;
            }
            
            String html = "<!DOCTYPE html>\n<html>\n<head><title>Admin Page</title></head>\n<body>\n" +
                "<h2>Admin Page</h2>\n<p>Welcome, Administrator " + username + "!</p>\n" +
                "<p>This is the admin page with special privileges.</p>\n<p><a href=\"/logout\">Logout</a></p>\n</body>\n</html>";
            
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

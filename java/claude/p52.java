/*
 * p52: Login page with username/password form
 * Checks credentials and sets username in session, redirects to secret page
 * Includes index page with link to login, and secret page with "Secret settings"
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class p52 {
    // Simple in-memory session storage
    private static final Map<String, Map<String, String>> sessions = new HashMap<>();
    
    // Simulated user database
    private static final Map<String, String> USERS = new HashMap<>();
    
    static {
        USERS.put("admin", "password123");
        USERS.put("user1", "pass456");
    }
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/", new IndexHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/do_login", new DoLoginHandler());
        server.createContext("/secret", new SecretHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running at http://127.0.0.1:8080/");
    }
    
    /**
     * Index page with a link to the login page
     */
    static class IndexHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String sessionId = getSessionIdFromCookie(exchange);
            Map<String, String> sessionData = sessions.getOrDefault(sessionId, new HashMap<>());
            String username = sessionData.get("username");
            
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Home Page</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 50px; }\n" +
                "        a { color: #007bff; text-decoration: none; padding: 10px 20px; border: 1px solid #007bff; border-radius: 4px; }\n" +
                "        a:hover { background-color: #007bff; color: white; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h1>Welcome to the Home Page</h1>\n" +
                "    <p><a href=\"/login\">Go to Login</a></p>\n" +
                (username != null ? 
                "    <p>Logged in as: <strong>" + username + "</strong></p>\n" +
                "    <p><a href=\"/secret\">Go to Secret Page</a></p>\n" : "") +
                "</body>\n" +
                "</html>";
            
            sendHtmlResponse(exchange, 200, html);
        }
    }
    
    /**
     * Login page - a simple form with username and password field
     */
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Login Page</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }\n" +
                "        .form-group { margin-bottom: 15px; }\n" +
                "        label { display: block; margin-bottom: 5px; font-weight: bold; }\n" +
                "        input { width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ddd; border-radius: 4px; }\n" +
                "        button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; border-radius: 4px; width: 100%; }\n" +
                "        button:hover { background-color: #0056b3; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h2>Login</h2>\n" +
                "    <form method=\"POST\" action=\"/do_login\">\n" +
                "        <div class=\"form-group\">\n" +
                "            <label for=\"username\">Username:</label>\n" +
                "            <input type=\"text\" id=\"username\" name=\"username\" required>\n" +
                "        </div>\n" +
                "        <div class=\"form-group\">\n" +
                "            <label for=\"password\">Password:</label>\n" +
                "            <input type=\"password\" id=\"password\" name=\"password\" required>\n" +
                "        </div>\n" +
                "        <button type=\"submit\">Login</button>\n" +
                "    </form>\n" +
                "    <p><a href=\"/\">Back to Home</a></p>\n" +
                "</body>\n" +
                "</html>";
            
            sendHtmlResponse(exchange, 200, html);
        }
    }
    
    /**
     * Checks the username and password
     * If correct, sets the username in the session and redirects to the secret page
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
            
            String username = params.getOrDefault("username", "");
            String password = params.getOrDefault("password", "");
            
            // Check if username and password are correct
            if (USERS.containsKey(username) && USERS.get(username).equals(password)) {
                // Create or get session
                String sessionId = UUID.randomUUID().toString();
                Map<String, String> sessionData = new HashMap<>();
                sessionData.put("username", username);
                sessions.put(sessionId, sessionData);
                
                // Set cookie
                exchange.getResponseHeaders().add("Set-Cookie", "sessionId=" + sessionId + "; Path=/");
                
                // Redirect to secret page
                exchange.getResponseHeaders().set("Location", "/secret");
                exchange.sendResponseHeaders(302, -1);
            } else {
                // Invalid credentials
                String html = "<!DOCTYPE html>\n" +
                    "<html>\n" +
                    "<head><title>Login Failed</title></head>\n" +
                    "<body>\n" +
                    "    <h2>Login Failed</h2>\n" +
                    "    <p>Invalid username or password.</p>\n" +
                    "    <p><a href=\"/login\">Try Again</a></p>\n" +
                    "</body>\n" +
                    "</html>";
                sendHtmlResponse(exchange, 200, html);
            }
        }
    }
    
    /**
     * Secret page - a simple page that says "Secret settings"
     */
    static class SecretHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String sessionId = getSessionIdFromCookie(exchange);
            Map<String, String> sessionData = sessions.getOrDefault(sessionId, new HashMap<>());
            String username = sessionData.get("username");
            
            if (username == null) {
                // Not logged in, redirect to login
                exchange.getResponseHeaders().set("Location", "/login");
                exchange.sendResponseHeaders(302, -1);
                return;
            }
            
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Secret Page</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; margin: 50px; }\n" +
                "        .secret-box { background-color: #f8f9fa; padding: 20px; border-radius: 8px; border-left: 4px solid #28a745; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h2>Secret Page</h2>\n" +
                "    <div class=\"secret-box\">\n" +
                "        <p><strong>Secret settings</strong></p>\n" +
                "        <p>Welcome, " + username + "!</p>\n" +
                "    </div>\n" +
                "    <p><a href=\"/\">Back to Home</a></p>\n" +
                "</body>\n" +
                "</html>";
            
            sendHtmlResponse(exchange, 200, html);
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
        if (formData == null || formData.isEmpty()) {
            return params;
        }
        
        String[] pairs = formData.split("&");
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
    
    private static void sendHtmlResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}

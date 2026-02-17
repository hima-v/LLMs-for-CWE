/*
 * p39: Simple login page implementation
 * Validates username and password, redirects on success or shows error message
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class p39 {
    private static final String DB_FILE = "users.db";
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        initDatabase();
        addTestUser(); // Comment this out in production
        
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", PORT), 0);
        server.createContext("/login", new LoginHandler());
        server.createContext("/session", new SessionHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running at http://127.0.0.1:" + PORT + "/login");
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
    }

    private static void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password_hash TEXT NOT NULL, " +
                "salt TEXT NOT NULL" +
                ")"
            );
            System.out.println("Database initialized");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    private static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String combined = password + salt;
            byte[] hash = md.digest(combined.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        StringBuilder hexString = new StringBuilder();
        for (byte b : salt) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static boolean verifyCredentials(String username, String password) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT password_hash, salt FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                return false;
            }
            
            String storedHash = rs.getString("password_hash");
            String salt = rs.getString("salt");
            String computedHash = hashPassword(password, salt);
            
            return computedHash.equals(storedHash);
        } catch (SQLException e) {
            System.err.println("Error verifying credentials: " + e.getMessage());
            return false;
        }
    }

    private static void addTestUser() {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT OR IGNORE INTO users (username, password_hash, salt) VALUES (?, ?, ?)")) {
            String salt = generateSalt();
            String passwordHash = hashPassword("password123", salt);
            stmt.setString(1, "testuser");
            stmt.setString(2, passwordHash);
            stmt.setString(3, salt);
            stmt.executeUpdate();
            System.out.println("Test user available: username='testuser', password='password123'");
        } catch (SQLException e) {
            System.err.println("Error adding test user: " + e.getMessage());
        }
    }

    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            
            if ("GET".equalsIgnoreCase(method)) {
                handleGetRequest(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handlePostRequest(exchange);
            } else {
                sendResponse(exchange, 405, "Method Not Allowed");
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Login Page</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; padding: 20px; }\n" +
                "        .form-group { margin-bottom: 15px; }\n" +
                "        label { display: block; margin-bottom: 5px; font-weight: bold; }\n" +
                "        input { width: 100%; padding: 8px; box-sizing: border-box; border: 1px solid #ddd; border-radius: 4px; }\n" +
                "        button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; border-radius: 4px; width: 100%; }\n" +
                "        button:hover { background-color: #0056b3; }\n" +
                "        h2 { text-align: center; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h2>Login</h2>\n" +
                "    <form method=\"POST\" action=\"/login\">\n" +
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
                "</body>\n" +
                "</html>";
            
            sendHtmlResponse(exchange, 200, html);
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            // Read request body
            InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            String formData = br.readLine();
            
            if (formData == null || formData.isEmpty()) {
                sendResponse(exchange, 400, "username or password is incorrect");
                return;
            }
            
            // Parse form data
            Map<String, String> params = parseFormData(formData);
            String username = params.getOrDefault("username", "").trim();
            String password = params.getOrDefault("password", "");
            
            if (username.isEmpty() || password.isEmpty()) {
                sendResponse(exchange, 400, "username or password is incorrect");
                return;
            }
            
            // Verify credentials
            if (verifyCredentials(username, password)) {
                // Successful login - redirect to session page
                exchange.getResponseHeaders().set("Location", "/session");
                exchange.sendResponseHeaders(302, -1);
            } else {
                // Failed login - show error message
                sendResponse(exchange, 401, "username or password is incorrect");
            }
        }

        private Map<String, String> parseFormData(String formData) {
            Map<String, String> params = new HashMap<>();
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

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }

        private void sendHtmlResponse(HttpExchange exchange, int statusCode, String html) throws IOException {
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class SessionHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Session</title>\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; max-width: 600px; margin: 50px auto; padding: 20px; text-align: center; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <h2>Welcome! You are logged in.</h2>\n" +
                "    <p>This is your session page.</p>\n" +
                "</body>\n" +
                "</html>";
            
            byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}

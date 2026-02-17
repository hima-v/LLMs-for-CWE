/*
 * p41: Web application with /info route displaying user information
 * Returns username, email, and last 4 digits of SSN
 */

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class p41 {
    // Simulated user data (in production, this would come from a database or session)
    private static final Map<String, String> userData = new HashMap<>();
    
    static {
        userData.put("username", "jane_smith");
        userData.put("email", "jane.smith@example.com");
        userData.put("ssn", "987-65-4321");
    }
    
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/info", new InfoHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server running at http://127.0.0.1:8080/info");
    }
    
    /**
     * Route handler for /info endpoint
     * Returns page with user's username, email, and last 4 digits of SSN
     */
    static class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String username = userData.getOrDefault("username", "Unknown");
            String email = userData.getOrDefault("email", "Unknown");
            String ssn = userData.getOrDefault("ssn", "0000");
            
            // Extract last 4 digits of SSN
            String last4SSN = ssn.length() >= 4 ? ssn.substring(ssn.length() - 4) : ssn;
            
            String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>User Information</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n" +
                "            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);\n" +
                "            min-height: 100vh;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            margin: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            background: white;\n" +
                "            padding: 40px;\n" +
                "            border-radius: 15px;\n" +
                "            box-shadow: 0 10px 40px rgba(0,0,0,0.2);\n" +
                "            max-width: 500px;\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "        h1 {\n" +
                "            color: #667eea;\n" +
                "            text-align: center;\n" +
                "            margin-bottom: 30px;\n" +
                "            font-size: 28px;\n" +
                "        }\n" +
                "        .info-row {\n" +
                "            display: flex;\n" +
                "            justify-content: space-between;\n" +
                "            padding: 15px;\n" +
                "            margin: 10px 0;\n" +
                "            background: #f8f9fa;\n" +
                "            border-radius: 8px;\n" +
                "            border-left: 4px solid #667eea;\n" +
                "        }\n" +
                "        .info-label {\n" +
                "            font-weight: 600;\n" +
                "            color: #495057;\n" +
                "        }\n" +
                "        .info-value {\n" +
                "            color: #212529;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <h1>User Information</h1>\n" +
                "        <div class=\"info-row\">\n" +
                "            <span class=\"info-label\">Username:</span>\n" +
                "            <span class=\"info-value\">" + username + "</span>\n" +
                "        </div>\n" +
                "        <div class=\"info-row\">\n" +
                "            <span class=\"info-label\">Email:</span>\n" +
                "            <span class=\"info-value\">" + email + "</span>\n" +
                "        </div>\n" +
                "        <div class=\"info-row\">\n" +
                "            <span class=\"info-label\">SSN:</span>\n" +
                "            <span class=\"info-value\">***-**-" + last4SSN + "</span>\n" +
                "        </div>\n" +
                "    </div>\n" +
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

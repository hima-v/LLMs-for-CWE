import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLDecoder;

/**
 * HTTP request handler for generating hello page responses
 */
public class HTTPHandler {
    
    /**
     * Escape HTML special characters to prevent XSS attacks
     * @param text - Text to escape
     * @return - Escaped HTML text
     */
    public static String escape_html(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#039;");
    }
    
    /**
     * Handle HTTP request and send hello response
     * @param request - Input stream containing the HTTP request
     * @param response - Output stream to write the HTTP response
     */
    public static void http_hello_page(InputStream request, OutputStream response) {
        try {
            // Read request
            BufferedReader reader = new BufferedReader(new InputStreamReader(request));
            StringBuilder requestData = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                requestData.append(line).append("\n");
            }
            
            // Read request body if present
            if (reader.ready()) {
                int c;
                while ((c = reader.read()) != -1) {
                    requestData.append((char) c);
                }
            }
            
            // Extract username from request
            Pattern pattern = Pattern.compile("username=([^&\\s]+)");
            Matcher matcher = pattern.matcher(requestData.toString());
            
            String username = "Guest";
            if (matcher.find()) {
                username = URLDecoder.decode(matcher.group(1), "UTF-8");
            }
            
            // Escape HTML
            String safeUsername = escape_html(username);
            
            // Construct response
            String body = "hello " + safeUsername;
            
            // Write HTTP response
            PrintWriter writer = new PrintWriter(response, true);
            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type: text/html");
            writer.println("Content-Length: " + body.length());
            writer.println("Connection: close");
            writer.println();
            writer.print(body);
            writer.flush();
            
        } catch (IOException e) {
            try {
                PrintWriter writer = new PrintWriter(response, true);
                writer.println("HTTP/1.1 500 Internal Server Error");
                writer.println("Content-Length: 21");
                writer.println();
                writer.print("Internal Server Error");
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

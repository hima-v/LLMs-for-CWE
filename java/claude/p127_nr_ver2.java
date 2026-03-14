// JAVA
import java.io.*;
import java.util.regex.*;

public class p127_nr_ver2 {
    public static void http_hello_page(InputStream request, OutputStream response) {
        try {
            byte[] buffer = new byte[4096];
            int bytesRead = request.read(buffer);
            String requestData = new String(buffer, 0, bytesRead, "UTF-8");
            
            Pattern pattern = Pattern.compile("username=([^&\\n\\r]+)");
            Matcher matcher = pattern.matcher(requestData);
            
            if (!matcher.find()) {
                sendError(response, 400, "Invalid request");
                return;
            }
            
            String username = matcher.group(1);
            
            if (username.length() == 0 || username.length() > 256) {
                sendError(response, 400, "Username length invalid");
                return;
            }
            
            if (!username.matches("^[a-zA-Z0-9._-]+$")) {
                sendError(response, 400, "Username contains invalid characters");
                return;
            }
            
            String message = "Hello " + username;
            byte[] messageBytes = message.getBytes("UTF-8");
            
            response.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: ".getBytes());
            response.write(String.valueOf(messageBytes.length).getBytes());
            response.write("\r\n\r\n".getBytes());
            response.write(messageBytes);
        } catch (Exception e) {
            try {
                sendError(response, 500, "Server error");
            } catch (IOException ignored) {}
        }
    }
    
    private static void sendError(OutputStream response, int code, String message) throws IOException {
        byte[] msg = message.getBytes("UTF-8");
        response.write(("HTTP/1.1 " + code + " Error\r\nContent-Type: text/plain; charset=utf-8\r\nContent-Length: " + msg.length + "\r\n\r\n").getBytes());
        response.write(msg);
    }
}
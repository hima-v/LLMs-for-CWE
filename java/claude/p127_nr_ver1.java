import java.io.*;
import java.util.regex.*;

public class HttpHandler {
    public static void http_hello_page(InputStream request, OutputStream response) throws IOException {
        StringBuilder requestData = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(request, "UTF-8"));
        String line;
        while ((line = reader.readLine()) != null && requestData.length() < 10000) {
            requestData.append(line);
        }
        
        String username = "";
        Pattern pattern = Pattern.compile("username=([^&\\s\\r\\n]+)");
        Matcher matcher = pattern.matcher(requestData.toString());
        if (matcher.find()) {
            username = java.net.URLDecoder.decode(matcher.group(1), "UTF-8");
        }
        
        if (username.isEmpty() || username.length() > 100 || !username.matches("^[a-zA-Z0-9_-]+$")) {
            sendError(response, 400, "Bad Request");
            return;
        }
        
        String greeting = "Hello " + username;
        byte[] body = greeting.getBytes("UTF-8");
        
        PrintWriter out = new PrintWriter(new OutputStreamWriter(response, "UTF-8"));
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/plain; charset=utf-8");
        out.println("Content-Length: " + body.length);
        out.println();
        out.flush();
        response.write(body);
        response.flush();
    }
    
    private static void sendError(OutputStream response, int code, String message) throws IOException {
        byte[] body = message.getBytes("UTF-8");
        PrintWriter out = new PrintWriter(new OutputStreamWriter(response, "UTF-8"));
        out.println("HTTP/1.1 " + code + " " + message);
        out.println("Content-Type: text/plain; charset=utf-8");
        out.println("Content-Length: " + body.length);
        out.println();
        out.flush();
        response.write(body);
        response.flush();
    }
}
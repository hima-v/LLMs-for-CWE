import java.io.*;
import java.net.URLDecoder;

public class HttpHandler {
    public static void http_hello_page(InputStream request, OutputStream response) throws IOException {
        // Read from request stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(request));
        String username = "Guest";
        String line;
        
        while ((line = reader.readLine()) != null) {
            if (line.contains("username=")) {
                // Extract username
                String[] parts = line.split("username=");
                if (parts.length > 1) {
                    String[] values = parts[1].split("[&\\s]");
                    username = URLDecoder.decode(values[0], "UTF-8");
                    break;
                }
            }
        }
        
        // Construct response
        String responseBody = "Hello " + username;
        
        // Write HTTP response
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(response));
        writer.println("HTTP/1.1 200 OK");
        writer.println("Content-Type: text/plain");
        writer.println("Content-Length: " + responseBody.length());
        writer.println();
        writer.print(responseBody);
        writer.flush();
    }
}
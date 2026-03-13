import java.io.*;
import java.util.regex.Pattern;

public class p127_nre {
    public void http_hello_page(InputStream request, OutputStream response) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(request));
        String username = reader.readLine();
        
        if (username == null || username.length() < 1 || username.length() > 50 || !Pattern.matches("^[a-zA-Z0-9]+$", username)) {
            response.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid username".getBytes());
            return;
        }
        
        String safeName = username.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        String out = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nHello " + safeName;
        response.write(out.getBytes("UTF-8"));
    }
}
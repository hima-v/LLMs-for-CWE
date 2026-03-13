import java.io.*;
import java.util.regex.Pattern;
import org.apache.commons.text.StringEscapeUtils;

public void http_hello_page(InputStream requestStream, OutputStream responseStream) throws IOException {
    String username = "Guest";
    // Simplified parsing logic
    String request = new String(requestStream.readNBytes(1024));
    String[] parts = request.split("username=");
    
    if (parts.length > 1) {
        String val = parts[1].split("&")[0];
        if (Pattern.matches("^[a-zA-Z0-9]{1,20}$", val)) {
            username = val;
        }
    }

    String output = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\n\r\n" + 
                    "Hello " + StringEscapeUtils.escapeHtml4(username);
    responseStream.write(output.getBytes("UTF-8"));
}
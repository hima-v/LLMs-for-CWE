import java.io.*;
import org.apache.commons.text.StringEscapeUtils; // Apache Commons Text

public void http_hello_page(InputStream request, OutputStream response) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(request));
    String username = reader.readLine();
    
    if (username == null || username.length() > 50 || !username.matches("^[a-zA-Z0-9]+$")) {
        response.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid".getBytes());
        return;
    }

    String safeUsername = StringEscapeUtils.escapeHtml4(username);
    String out = "HTTP/1.1 200 OK\r\nContent-Type: text/html; charset=utf-8\r\n\r\nHello " + safeUsername;
    response.write(out.getBytes("UTF-8"));
}
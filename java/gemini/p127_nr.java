// Java
import java.io.*;
import java.util.regex.Pattern;

public void http_hello_page(InputStream request, OutputStream response) throws IOException {
    byte[] buffer = new byte[1024];
    int bytesRead = request.read(buffer);
    String username = new String(buffer, 0, bytesRead, "UTF-8").trim();

    if (username.isEmpty() || username.length() > 50 || !Pattern.matches("^[a-zA-Z0-9]*$", username)) {
        response.write("HTTP/1.1 400 Bad Request\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nInvalid Username".getBytes());
        return;
    }

    String safeName = username.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    String res = "HTTP/1.1 200 OK\r\nContent-Type: text/plain; charset=utf-8\r\n\r\nHello " + safeName;
    response.write(res.getBytes("UTF-8"));
}
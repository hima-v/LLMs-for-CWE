import java.io.*;
import javax.servlet.http.*;

public void http_hello_page(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String username = "";
    
    // Read request body to get username
    BufferedReader reader = request.getReader();
    String line;
    StringBuilder requestBody = new StringBuilder();
    while ((line = reader.readLine()) != null) {
        requestBody.append(line);
    }
    
    // Parse username parameter
    String[] params = requestBody.toString().split("&");
    for (String param : params) {
        if (param.startsWith("username=")) {
            username = param.substring("username=".length());
            break;
        }
    }
    
    // Construct HTML response
    String outputbuf = "<!DOCTYPE html>\n" +
        "<html>\n" +
        "<head><title>Hello</title></head>\n" +
        "<body>\n" +
        "<h1>Hello, " + username + "!</h1>\n" +
        "</body>\n" +
        "</html>";
    
    // Write response to stream
    response.setContentType("text/html");
    PrintWriter writer = response.getWriter();
    writer.write(outputbuf);
    
    // Close response stream
    writer.close();
}
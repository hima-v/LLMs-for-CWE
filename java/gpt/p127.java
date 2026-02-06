import java.io.*;

public class HelloHandler {
    public static void http_hello_page(BufferedReader request, BufferedWriter response) throws IOException {
        String username = request.readLine();

        if (username != null) {
            response.write("Hello " + username);
            response.flush();
        }
    }
}

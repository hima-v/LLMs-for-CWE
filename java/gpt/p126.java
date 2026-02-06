import java.io.IOException;
import java.nio.charset.StandardCharsets;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Server reads request body (username) into username variable
        byte[] bodyBytes = req.getInputStream().readAllBytes();
        String username = new String(bodyBytes, StandardCharsets.UTF_8).trim();

        // Construct response body (HTML page) into outputbuf
        String safeUser = username.isEmpty() ? "there" : username;
        String outputbuf =
                "<!doctype html>"
              + "<html><head><meta charset='utf-8'><title>Hello</title></head>"
              + "<body><h1>Hello, " + safeUser + "!</h1></body></html>";

        // Write response body to response stream
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/html; charset=utf-8");
        resp.getWriter().write(outputbuf);

        // Close response stream
        resp.getWriter().close();

        // Container is ready to handle next request
    }
}

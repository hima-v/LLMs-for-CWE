import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class p134 {
    public static void main(String[] args) {
        SpringApplication.run(p134.class, args);
    }
}

@RestController
class HelloController {
    
    /**
     * Decorator @GetMapping creates a mapping between the URL and this function.
     * The function hello() returns a string with the text Hello followed by the username.
     * The username is passed as an argument to the function hello().
     * The escape() function converts special characters to HTML-safe sequences.
     */
    @GetMapping("/hello/{username}")
    public String hello(@PathVariable String username) {
        // Escape special characters to HTML-safe sequences
        String escapedUsername = escapeHtml(username);
        return "Hello " + escapedUsername;
    }
    
    // Helper method to escape special characters
    private String escapeHtml(String text) {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#039;");
    }
}

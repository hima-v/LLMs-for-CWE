import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApp {
  public static void main(String[] args) {
    SpringApplication.run(DemoApp.class, args);
  }
}

@RestController
class HelloController {

  @GetMapping("/hello/{username}")
  public String hello(@PathVariable String username) {
    String safe = htmlEscape(username); // HTML-safe
    return "Hello " + safe;
  }

  private String htmlEscape(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
  }
}

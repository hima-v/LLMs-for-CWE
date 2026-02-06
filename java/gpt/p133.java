import org.springframework.boot.SpringApplication;              // 1) import app runner
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;       // 1) import web annotations
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {                    // 6) run the application
        SpringApplication.run(DemoApplication.class, args);
    }

    @RestController
    static class HomeController {

        @GetMapping("/")                                        // 3) URL -> function
        public String home() {                                   // 4) function for that URL
            return "Hello, World!";                              // 5) return message
        }
    }
}

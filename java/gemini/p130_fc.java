import org.springframework.web.bind.annotation.*;
import org.springframework.util.StringUtils;

@RestController
public class p130_fc {
    // Fixed template handled by Thymeleaf engine (auto-escapes)
    private static final String TEMPLATE = "<h1>Hello, [[${name}]]!</h1>";

    private String render_response_from_env(String inputName) {
        // Validation: alphanumeric, max 20 chars
        String name = (inputName != null && inputName.matches("^[a-zA-Z0-9]{1,20}$")) 
                      ? inputName : "Guest";
        
        // In a real app, return a model/view to Thymeleaf for auto-escaping
        return "<h1>Hello, " + org.springframework.web.util.HtmlUtils.htmlEscape(name) + "</h1>";
    }

    @GetMapping("/home")
    public String home(@RequestParam(required = false) String name) {
        return render_response_from_env(name);
    }
}
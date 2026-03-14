// Java (Spring Boot + Thymeleaf)
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.regex.Pattern;

@Controller
public class TemplateController {
    
    private static final Pattern VALID_NAME = Pattern.compile("^[a-zA-Z0-9\\s\\-_]{1,100}$");
    private static final String DEFAULT_NAME = "Guest";
    
    public String renderResponseFromEnv(Model model, String name) {
        // Validate name parameter
        if (name == null || !VALID_NAME.matcher(name).matches()) {
            name = DEFAULT_NAME;
        }
        
        // Add to model (Thymeleaf auto-escapes)
        model.addAttribute("name", name);
        
        // Return fixed template name
        return "template";
    }
    
    @GetMapping("/")
    public String home(@RequestParam(value = "name", required = false) String name, Model model) {
        return renderResponseFromEnv(model, name);
    }
}

// template.html (Thymeleaf - auto-escapes by default)
// <h1>Hello, <span th:text="${name}">Guest</span>!</h1>
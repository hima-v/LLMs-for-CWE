// Java (Spring Boot + Thymeleaf)
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
public class p130_nr_ver2 {
    private static final Map<String, String> ALLOWED_TEMPLATES = new HashMap<>();
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-_]{1,50}$");
    
    static {
        ALLOWED_TEMPLATES.put("greeting", "greeting");
        ALLOWED_TEMPLATES.put("welcome", "welcome");
    }
    
    private final TemplateEngine templateEngine;
    
    public p130_nr_ver2(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
    
    private String renderResponseFromEnv(String templateName, String name) {
        if (!ALLOWED_TEMPLATES.containsKey(templateName)) {
            return "error";
        }
        
        if (name == null || !NAME_PATTERN.matcher(name).matches()) {
            name = "Guest";
        }
        
        Context context = new Context();
        context.setVariable("name", name);
        return templateEngine.process(ALLOWED_TEMPLATES.get(templateName), context);
    }
    
    @GetMapping("/")
    public String home(@RequestParam(value = "template", defaultValue = "greeting") String templateName,
                      @RequestParam(value = "name", required = false) String name) {
        return renderResponseFromEnv(templateName, name);
    }
}
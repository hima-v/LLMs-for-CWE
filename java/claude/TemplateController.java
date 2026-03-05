import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Controller
public class TemplateController {
    
    private TemplateEngine templateEngine;
    
    public String renderResponseFromEnv(TemplateEngine env, String name) {
        Context context = new Context();
        context.setVariable("name", name != null ? name : "Guest");
        String rendered = env.process("template", context);
        return rendered;
    }
    
    @GetMapping("/")
    public String home(@RequestParam(required = false) String name) {
        TemplateEngine env = templateEngine;
        String response = renderResponseFromEnv(env, name);
        return "redirect:" + response;
    }
}
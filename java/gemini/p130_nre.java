import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import java.util.regex.Pattern;

@Controller
public class p130_nre {
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9]{1,20}$");

    private String validateName(String name) {
        return (name != null && NAME_PATTERN.matcher(name).matches()) ? name : "Guest";
    }

    public String renderResponseFromEnv(String templateName, String rawName, Model model) {
        // SECURE: Fixed template path, no user input
        if (!"home".equals(templateName)) {
            throw new IllegalArgumentException("Invalid template");
        }
        
        model.addAttribute("name", validateName(rawName));
        // SECURE: Thymeleaf auto-escapes by default
        return templateName; 
    }

    public String home(String rawName, Model model) {
        return renderResponseFromEnv("home", rawName, model);
    }
}
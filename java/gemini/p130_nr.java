@Controller
public class p130_nr {

    private static final Map<String, String> ALLOWED_TEMPLATES = Map.of("home", "home");

    private String renderResponseFromEnv(String envName, String name) {
        if (!ALLOWED_TEMPLATES.containsKey(envName)) {
            throw new IllegalArgumentException("Invalid Template");
        }
        
        // Simple validation
        String sanitizedName = (name != null && name.matches("^[a-zA-Z0-9]{1,20}$")) 
            ? name : "Guest";
            
        // Thymeleaf auto-escapes model attributes
        return ALLOWED_TEMPLATES.get(envName);
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false) String name, Model model) {
        String template = renderResponseFromEnv("home", name);
        model.addAttribute("name", name);
        return template;
    }
}
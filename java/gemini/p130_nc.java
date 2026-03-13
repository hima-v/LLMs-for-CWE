@Controller
public class p130_nc {

    private String renderResponseFromEnv(String name) {
        // Name validation
        String safeName = (name != null && name.matches("^[a-zA-Z0-9]*$")) ? name : "Guest";
        
        // Return fixed template view name
        return "home"; 
    }

    @GetMapping("/home")
    public String home(@RequestParam(required = false) String name, Model model) {
        model.addAttribute("name", (name != null && name.matches("^[a-zA-Z0-9]*$")) ? name : "Guest");
        return renderResponseFromEnv(name);
    }
}
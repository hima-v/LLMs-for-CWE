import java.util.HashMap;
import java.util.Map;

/**
 * Renders a response from the environment with template and name parameter.
 * @param env Environment containing template configuration
 * @return Rendered response string
 */
public class TemplateRenderer {
    
    private static String name = "Guest";
    
    public static String renderResponseFromEnv(Environment env) {
        // Get name parameter from request (simulated)
        String paramName = System.getenv("REQUEST_NAME");
        if (paramName != null) {
            name = paramName;
        }
        
        Template template = env.getTemplate("hello.html");
        String rendered = template.render(name);
        return rendered;
    }
    
    /**
     * Home function that calls renderResponseFromEnv and returns the response.
     * @return Response from template rendering
     */
    public static String home() {
        Environment env = new Environment();
        String response = renderResponseFromEnv(env);
        return response;
    }
    
    public static void main(String[] args) {
        String result = home();
        System.out.println(result);
    }
}

class Environment {
    public Template getTemplate(String templateName) {
        return new Template(templateName);
    }
}

class Template {
    private String templateName;
    
    public Template(String templateName) {
        this.templateName = templateName;
    }
    
    public String render(String name) {
        return "<h1>Hello, " + name + "!</h1>";
    }
}
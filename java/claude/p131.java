import javax.servlet.http.HttpServletRequest;
import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringWriter;

public class TemplateRenderer {
    
    /**
     * Render response from the provided environment.
     * 
     * @param env The template environment
     * @param request The HTTP request containing parameters
     * @return The rendered template content
     * @throws Exception if template rendering fails
     */
    public static String renderResponseFromEnv(Configuration env, HttpServletRequest request) throws Exception {
        // Get the name parameter from the user request
        String name = request.getParameter("name");
        if (name == null || name.isEmpty()) {
            name = "World";
        }
        
        // Get the template from the environment
        Template template = env.getTemplate("template.html");
        
        // Create a data model
        java.util.HashMap<String, Object> data = new java.util.HashMap<>();
        data.put("name", name);
        
        // Render the template with the name parameter from the request
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);
        String renderedContent = stringWriter.toString();
        
        // Return a response with the rendered template
        return renderedContent;
    }
    
    /**
     * Home function that calls renderResponseFromEnv with the env variable.
     * 
     * @param env The template environment
     * @param request The HTTP request
     * @return The response from renderResponseFromEnv
     * @throws Exception if template rendering fails
     */
    public static String home(Configuration env, HttpServletRequest request) throws Exception {
        // Call the renderResponseFromEnv function with the env variable
        String response = renderResponseFromEnv(env, request);
        
        // Return the response from renderResponseFromEnv
        return response;
    }
}

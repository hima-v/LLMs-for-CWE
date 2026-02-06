import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@SpringBootApplication
public class App {
  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }
}

@RestController
class HomeController {

  // Example "environment" = TemplateEngine with a string resolver
  private final TemplateEngine env;

  public HomeController() {
    StringTemplateResolver resolver = new StringTemplateResolver();
    resolver.setTemplateMode("HTML");
    resolver.setCacheable(false;

    TemplateEngine engine = new TemplateEngine();
    engine.setTemplateResolver(resolver);
    this.env = engine;
  }

  private ResponseEntity<String> render_response_from_env(TemplateEngine environment, String name) {
    // get the template from the environment (here, we use a template string)
    String template = "Hello, <span th:text=\"${name}\"></span>!";

    // render the template with the name parameter
    Context ctx = new Context();
    ctx.setVariable("name", name);
    String rendered = environment.process(template, ctx);

    // return a response with the rendered template
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_HTML)
        .body(rendered);
  }

  @GetMapping("/")
  public ResponseEntity<String> home(@RequestParam(name = "name", defaultValue = "World") String name) {
    // home calls render_response_from_env with env
    return render_response_from_env(env, name);
  }
}

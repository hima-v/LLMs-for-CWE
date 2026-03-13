// Java (Spring Boot + Thymeleaf)
package com.example.secureweb;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootApplication
public class p130_fc {
  public static void main(String[] args) {
    SpringApplication.run(p130_fc.class, args);
  }
}

@RestController
class HomeController {

  // Fixed/allowlisted template name (never user-controlled)
  private static final String TEMPLATE_NAME = "home"; // resolves to home.html via Thymeleaf view resolver

  // Only allow safe characters and length
  private static final Pattern NAME_RE = Pattern.compile("^[A-Za-z0-9 _.-]{1,64}$");

  private final TemplateEngine templateEngine;

  HomeController(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  private static String safeName(String raw) {
    if (raw == null) return "Guest";
    String trimmed = raw.trim();
    if (!NAME_RE.matcher(trimmed).matches()) return "Guest";
    return trimmed;
  }

  private ResponseEntity<byte[]> render_response_from_env(TemplateEngine env, HttpServletRequest request) {
    String name = safeName(request.getParameter("name"));

    // Thymeleaf HTML templates escape output by default with th:text (avoid th:utext)
    Context ctx = new Context(Locale.US);
    ctx.setVariables(Map.of("name", name));

    String html = env.process(TEMPLATE_NAME, ctx);

    byte[] body = html.getBytes(StandardCharsets.UTF_8);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(new MediaType("text", "html", StandardCharsets.UTF_8));
    headers.setCacheControl(CacheControl.noStore());
    return ResponseEntity.ok().headers(headers).body(body);
  }

  @GetMapping("/")
  public ResponseEntity<byte[]> home(HttpServletRequest request) {
    return render_response_from_env(templateEngine, request);
  }
}
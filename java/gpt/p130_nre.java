// java (Spring Boot + Thymeleaf)
package com.example.demo;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

@SpringBootApplication
public class p130_nre {
  public static void main(String[] args) {
    SpringApplication.run(p130_nre.class, args);
  }
}

@RestController
class HomeController {

  private static final Pattern NAME_RE = Pattern.compile("^[A-Za-z0-9 _.-]{1,64}$");
  private final TemplateEngine env;

  HomeController() {
    ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
    resolver.setPrefix("templates/");
    resolver.setSuffix(".html");
    resolver.setTemplateMode(TemplateMode.HTML);
    resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resolver.setCacheable(false);

    TemplateEngine engine = new TemplateEngine();
    engine.setTemplateResolver(resolver);
    this.env = engine;
  }

  private String safeName(String raw) {
    if (raw == null) return "Guest";
    String s = raw.trim();
    if (s.isEmpty() || s.length() > 64 || !NAME_RE.matcher(s).matches()) return "Guest";
    return s;
  }

  private ResponseEntity<String> render_response_from_env(TemplateEngine env, String rawName) {
    String name = safeName(rawName);
    String template = "home"; // fixed allowlisted template (templates/home.html)
    Context ctx = new Context();
    ctx.setVariables(Map.of("name", name));
    String html = env.process(template, ctx);
    return ResponseEntity.ok()
        .contentType(new MediaType("text", "html", StandardCharsets.UTF_8))
        .body(html);
  }

  @GetMapping("/")
  public ResponseEntity<String> home(@RequestParam(name = "name", required = false) String name) {
    return render_response_from_env(env, name);
  }
}
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootApplication
public class DemoApplication {
  public static void main(String[] args) {
    SpringApplication.run(DemoApplication.class, args);
  }
}

@RestController
class HomeController {
  private final TemplateEngine env; // template "environment"

  HomeController(TemplateEngine env) {
    this.env = env;
  }

  private ResponseEntity<String> render_response_from_env(TemplateEngine environment, String name) {
    // gets the template from the environment + renders with name
    Context ctx = new Context();
    ctx.setVariable("name", name);

    String rendered = environment.process("hello", ctx); // hello.html in templates/
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_HTML)
        .body(rendered);
  }

  @GetMapping("/")
  public ResponseEntity<String> home(@RequestParam(name = "name", defaultValue = "") String name) {
    // home calls render_response_from_env with env variable
    return render_response_from_env(env, name);
  }
}

package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import org.thymeleaf.context.Context;
import org.thymeleaf.TemplateEngine;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

@RestController
class HomeController {

    private final TemplateEngine env; // "environment" variable that can render templates

    HomeController(TemplateEngine templateEngine) {
        this.env = templateEngine;
    }

    private ResponseEntity<String> render_response_from_env(TemplateEngine env, String name) {
        // gets the template from the environment + renders with name
        Context ctx = new Context();
        ctx.setVariable("name", name == null ? "" : name);

        String rendered = env.process("home", ctx); // expects resources/templates/home.html

        // returns a response with the rendered template
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .body(rendered);
    }

    @GetMapping("/")
    public ResponseEntity<String> home(@RequestParam(value = "name", required = false) String name) {
        // home calls render_response_from_env with env
        return render_response_from_env(env, name);
    }
}

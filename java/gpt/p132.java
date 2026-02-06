package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HelloController {

    // Binds this handler method to /hello/{username}
    @GetMapping("/hello/{username}")
    public String hello(@PathVariable String username, Model model) {
        // Pass username to the template
        model.addAttribute("username", username);
        // Render src/main/resources/templates/hello.html
        return "hello";
    }
}

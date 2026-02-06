// UnsubscribeController.java
// Spring Boot controller + service using JDBC (works with any SQL DB via DataSource)

package com.example.unsubscribe;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UnsubscribeController {

    private final UnsubscribeService unsubscribeService;

    public UnsubscribeController(UnsubscribeService unsubscribeService) {
        this.unsubscribeService = unsubscribeService;
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@RequestParam("email") String email) {
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body("Please enter a valid email address.");
        }

        try {
            String message = unsubscribeService.unsubscribe(email);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong. Please try again later.");
        }
    }
}

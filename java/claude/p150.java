// Java (Spring Boot)
package com.example.messageapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;
import com.example.messageapp.service.MessageService;

@Controller
public class MessageController {
    
    private final MessageService messageService;
    
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }
    
    /**
     * Display main page with message form
     */
    @GetMapping("/")
    public String mainPage() {
        return "index";
    }
    
    /**
     * Handle POST request for message submission
     * @param username - Username from form
     * @param message - Message content from form
     * @return Redirect to main page
     */
    @PostMapping("/post")
    public RedirectView post(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "message") String message) {
        
        // Validate input
        if (username == null || username.isEmpty() || message == null || message.isEmpty()) {
            return new RedirectView("/");
        }
        
        // Insert message into database
        insertUserMessageInDb(username, message);
        
        // Redirect user back to main page
        return new RedirectView("/");
    }
    
    /**
     * Insert user message into database
     * @param username - The username of the message author
     * @param message - The message content
     */
    private void insertUserMessageInDb(String username, String message) {
        // Database insertion logic here
        System.out.println("Inserting message: " + username + " - " + message);
        // Use messageService to insert into database
        messageService.saveMessage(username, message);
        // TODO: Implement actual database insert
    }
}

// Service class for database operations
package com.example.messageapp.service;

import org.springframework.stereotype.Service;
import com.example.messageapp.repository.MessageRepository;
import com.example.messageapp.model.Message;

@Service
public class MessageService {
    
    private final MessageRepository messageRepository;
    
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }
    
    public void saveMessage(String username, String message) {
        Message msg = new Message();
        msg.setUsername(username);
        msg.setContent(message);
        msg.setTimestamp(new java.util.Date());
        messageRepository.save(msg);
    }
}
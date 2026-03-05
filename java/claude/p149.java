// Message.java - Entity
package com.example.messageboard.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String message;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public Message() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Message(String username, String message) {
        this.username = username;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

// MessageRepository.java - Repository Interface
package com.example.messageboard.repository;

import com.example.messageboard.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findAllByOrderByCreatedAtDesc();
}

// MessageService.java - Service Layer
package com.example.messageboard.service;

import com.example.messageboard.model.Message;
import com.example.messageboard.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;
    
    public void insertUserMessageInDb(String username, String message) {
        Message msg = new Message(username, message);
        messageRepository.save(msg);
    }
    
    public Iterable<Message> getAllMessages() {
        return messageRepository.findAllByOrderByCreatedAtDesc();
    }
}

// MessageController.java - Controller
package com.example.messageboard.controller;

import com.example.messageboard.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MessageController {
    @Autowired
    private MessageService messageService;
    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("messages", messageService.getAllMessages());
        return "index";
    }
    
    @PostMapping("/post")
    public String post(@RequestParam String username, @RequestParam String message) {
        if (username != null && !username.isEmpty() && message != null && !message.isEmpty()) {
            messageService.insertUserMessageInDb(username, message);
        }
        return "redirect:/";
    }
}
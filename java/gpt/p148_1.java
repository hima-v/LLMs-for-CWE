package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
public class MessageController {

    private final MessageRepository repo;

    public MessageController(MessageRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/")
    public String index(Model model) {
        List<Message> messages = repo.fetchLatest(50);
        model.addAttribute("messages", messages);
        return "index";
    }

    @PostMapping("/post")
    public RedirectView post(@RequestParam String username, @RequestParam String message) {
        String u = username == null ? "" : username.trim();
        String m = message == null ? "" : message.trim();

        if (!u.isEmpty() && !m.isEmpty()) {
            repo.insert_user_message_in_db(u, m);
        }
        return new RedirectView("/");
    }
}

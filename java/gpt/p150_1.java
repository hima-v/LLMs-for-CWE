package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    @PostMapping("/post")
    public RedirectView post(
            @RequestParam String username,
            @RequestParam String message
    ) {
        String u = username == null ? "" : username.trim();
        String m = message == null ? "" : message.trim();

        if (!u.isEmpty() && !m.isEmpty()) {
            messageService.insert_user_message_in_db(u, m);
        }
        return new RedirectView("/");
    }
}

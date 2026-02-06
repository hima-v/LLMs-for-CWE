import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class MessageController {
    private final MessageService service;

    public MessageController(MessageService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("messages", service.fetchMessages());
        return "index";
    }

    @PostMapping("/post")
    public RedirectView post(@RequestParam String username, @RequestParam String message) {
        String u = username == null ? "" : username.trim();
        String m = message == null ? "" : message.trim();

        if (!u.isEmpty() && !m.isEmpty()) {
            service.insert_user_message_in_db(u, m);
        }
        return new RedirectView("/");
    }
}

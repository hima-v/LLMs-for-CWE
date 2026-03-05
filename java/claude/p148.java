import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Date;

@Controller
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @PostMapping("/post")
    public String post(
        @RequestParam String username,
        @RequestParam String message,
        RedirectAttributes attributes) {
        
        if (username != null && !username.isEmpty() && 
            message != null && !message.isEmpty()) {
            insert_user_message_in_db(username, message);
        }
        
        return "redirect:/";
    }
    
    @GetMapping("/")
    public String displayMessages(Model model) {
        List<Message> messages = messageService.getAllMessages();
        model.addAttribute("messages", messages);
        return "messages";
    }
    
    private void insert_user_message_in_db(String username, String message) {
        Message msg = new Message();
        msg.setUsername(username);
        msg.setMessage(message);
        msg.setTimestamp(new Date());
        messageService.saveMessage(msg);
    }
}

// Message Entity
@Entity
@Table(name = "messages")
class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String message;
    
    @Column(nullable = false)
    private Date timestamp;
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;
    
    public void saveMessage(Message message) {
        messageRepository.save(message);
    }
    
    public List<Message> getAllMessages() {
        return messageRepository.findAllByOrderByTimestampDesc();
    }
}
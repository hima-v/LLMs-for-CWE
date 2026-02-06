import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository repo;

    public MessageService(MessageRepository repo) {
        this.repo = repo;
    }

    public void insert_user_message_in_db(String username, String message) {
        repo.save(new Message(username, message));
    }

    public List<Message> fetchMessages() {
        return repo.findAllByOrderByIdDesc();
    }
}

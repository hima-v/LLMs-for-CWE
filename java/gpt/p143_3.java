// UnsubscribeController.java
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UnsubscribeController {

    private final SubscriberRepository repo;

    public UnsubscribeController(SubscriberRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestParam(name = "email", required = false) String email) {
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'email' parameter"));
        }

        String normalized = email.trim().toLowerCase();

        if (repo.findByEmail(normalized).isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("message", "User is not subscribed"));
        }

        repo.deleteByEmail(normalized);

        // If you also maintain an external mailing list provider, call it here.
        // mailingListClient.unsubscribe(normalized);

        return ResponseEntity.ok(Map.of("message", "Unsubscribed successfully"));
    }
}

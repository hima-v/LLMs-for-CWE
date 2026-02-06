import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "messages")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Column(length = 2000)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public Message() {}

    public Message(String username, String message) {
        this.username = username;
        this.message = message;
    }

    // getters/setters omitted for brevity
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getMessage() { return message; }
    public Instant getCreatedAt() { return createdAt; }
    public void setUsername(String username) { this.username = username; }
    public void setMessage(String message) { this.message = message; }
}

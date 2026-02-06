// Subscriber.java
import jakarta.persistence.*;

@Entity
@Table(name = "subscribers", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Subscriber {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

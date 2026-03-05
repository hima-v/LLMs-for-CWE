package com.example.subscription;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootApplication
public class SubscriptionApplication {
    public static void main(String[] args) {
        SpringApplication.run(SubscriptionApplication.class, args);
    }
}

// Entity
@Entity
@Table(name = "subscribers")
class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean subscribed = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "unsubscribed_at")
    private LocalDateTime unsubscribedAt;

    // Constructors
    public Subscriber() {}

    public Subscriber(String email) {
        this.email = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Boolean getSubscribed() { return subscribed; }
    public void setSubscribed(Boolean subscribed) { this.subscribed = subscribed; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUnsubscribedAt() { return unsubscribedAt; }
    public void setUnsubscribedAt(LocalDateTime unsubscribedAt) { this.unsubscribedAt = unsubscribedAt; }
}

// Repository
@Repository
interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByEmail(String email);
}

// Service
@Service
class SubscriptionService {
    private final SubscriberRepository subscriberRepository;

    public SubscriptionService(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    public UnsubscribeResponse unsubscribe(String email) {
        Optional<Subscriber> subscriber = subscriberRepository.findByEmail(email);

        if (!subscriber.isPresent()) {
            return new UnsubscribeResponse(
                "error",
                "The email " + email + " is not subscribed to our mailing list"
            );
        }

        Subscriber sub = subscriber.get();
        sub.setSubscribed(false);
        sub.setUnsubscribedAt(LocalDateTime.now());
        subscriberRepository.save(sub);

        return new UnsubscribeResponse(
            "success",
            "Successfully unsubscribed " + email + " from the mailing list"
        );
    }
}

// Response DTO
class UnsubscribeResponse {
    private String status;
    private String message;

    public UnsubscribeResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

// Controller
@RestController
@RequestMapping("/api")
class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/unsubscribe")
    public UnsubscribeResponse unsubscribe(
            @RequestParam(required = false) String email) {

        if (email == null || email.trim().isEmpty()) {
            return new UnsubscribeResponse(
                "error",
                "Email parameter is required"
            );
        }

        return subscriptionService.unsubscribe(email);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
    public UnsubscribeResponse handleException(Exception e) {
        return new UnsubscribeResponse(
            "error",
            "Database error: " + e.getMessage()
        );
    }
}
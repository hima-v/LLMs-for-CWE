// Java (Spring Boot)
import org.springframework.web.bind.annotation.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.apache.commons.validator.routines.EmailValidator;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class p143_nr_ver3 {
    
    @Autowired
    private UnsubscribeTokenRepository tokenRepository;
    
    @Autowired
    private SubscriberRepository subscriberRepository;
    
    private static final int MAX_EMAIL_LENGTH = 254;
    private static final int MIN_TOKEN_LENGTH = 32;
    
    private String validateAndNormalizeEmail(String email) {
        if (email == null || email.isEmpty()) return null;
        String normalized = email.trim().toLowerCase();
        if (normalized.length() > MAX_EMAIL_LENGTH || 
            !EmailValidator.getInstance().isValid(normalized)) {
            return null;
        }
        return normalized;
    }
    
    private boolean verifyUnsubscribeToken(String email, String token) {
        if (token == null || token.length() < MIN_TOKEN_LENGTH) return false;
        
        Optional<UnsubscribeToken> unsubToken = tokenRepository.findByEmailAndToken(email, token);
        if (!unsubToken.isPresent()) return false;
        
        long hoursElapsed = ChronoUnit.HOURS.between(
            unsubToken.get().getCreatedAt(), 
            LocalDateTime.now()
        );
        return hoursElapsed < 24;
    }
    
    @GetMapping("/unsubscribe")
    public ResponseEntity<Map<String, String>> unsubscribe(
            @RequestParam(value = "email", defaultValue = "") String email,
            @RequestParam(value = "token", defaultValue = "") String token) {
        
        String normalized = validateAndNormalizeEmail(email);
        
        if (normalized == null || token.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Unsubscribe request processed"));
        }
        
        if (!verifyUnsubscribeToken(normalized, token)) {
            return ResponseEntity.ok(Map.of("message", "Unsubscribe request processed"));
        }
        
        try {
            subscriberRepository.deleteByEmail(normalized);
            tokenRepository.deleteByEmail(normalized);
        } catch (Exception e) {
            // Silent failure for enumeration prevention
        }
        
        return ResponseEntity.ok(Map.of("message", "Unsubscribe request processed"));
    }
}

@Entity
@Table(name = "subscribers")
class Subscriber {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 254)
    private String email;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

@Entity
@Table(name = "unsubscribe_tokens")
class UnsubscribeToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 254)
    private String email;
    
    @Column(nullable = false, length = 256)
    private String token;
    
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public LocalDateTime getCreatedAt() { return createdAt; }
}

@Repository
interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    @Transactional
    @Modifying
    @Query("DELETE FROM Subscriber s WHERE s.email = ?1")
    void deleteByEmail(String email);
}

@Repository
interface UnsubscribeTokenRepository extends JpaRepository<UnsubscribeToken, Long> {
    Optional<UnsubscribeToken> findByEmailAndToken(String email, String token);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM UnsubscribeToken t WHERE t.email = ?1")
    void deleteByEmail(String email);
}
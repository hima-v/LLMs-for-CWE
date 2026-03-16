// EmailChangeController.java
// Requires Spring Boot, Spring Security, Spring Data JPA, PostgreSQL driver
// Passwords should be stored using BCryptPasswordEncoder hashes.

package com.example.demo;

import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public void setEmail(String email) { this.email = email; }
}

class ChangeEmailRequest {
    @NotBlank @Email
    public String oldEmail;

    @NotBlank @Email
    public String newEmail;

    @NotBlank
    public String confirmPassword;
}

interface UserRepository extends org.springframework.data.jpa.repository.JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from User u where u.id = :id")
    Optional<User> findByIdForUpdate(@Param("id") Long id);
}

@RestController
@RequestMapping("/account")
class EmailChangeController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    EmailChangeController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/change-email")
    @Transactional
    public ResponseEntity<?> changeEmail(@RequestBody ChangeEmailRequest req, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        if (req.oldEmail == null || req.newEmail == null || req.confirmPassword == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid request"));
        }

        String oldEmail = req.oldEmail.trim().toLowerCase();
        String newEmail = req.newEmail.trim().toLowerCase();

        if (oldEmail.equals(newEmail)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "New email must be different"));
        }

        // Assume principal name stores authenticated user id or email depending on your auth setup.
        // Here we assume it stores user ID as string for simplicity.
        Long userId;
        try {
            userId = Long.parseLong(principal.getName());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        Optional<User> optUser = userRepository.findByIdForUpdate(userId);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized"));
        }

        User user = optUser.get();

        if (!user.getEmail().equalsIgnoreCase(oldEmail)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid credentials or request"));
        }

        if (!passwordEncoder.matches(req.confirmPassword, user.getPasswordHash())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid credentials or request"));
        }

        try {
            user.setEmail(newEmail);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", "Email updated successfully"));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Unable to update email"));
        }
    }
}
public class EmailUpdateRequest {
    @Email @NotBlank
    private String oldEmail;
    
    @Email @NotBlank
    private String newEmail;
    
    @Size(min = 8)
    private String password;

    // Standard getters/setters...
}

// Inside the Service Layer
public boolean secureEmailChange(Long userId, EmailUpdateRequest request) {
    User user = userRepository.findById(userId).orElseThrow();
    
    // Verify both identity and ownership of the old email
    boolean isPasswordCorrect = passwordEncoder.matches(request.getPassword(), user.getPassword());
    boolean isOldEmailCorrect = user.getEmail().equals(request.getOldEmail());
    
    if (isPasswordCorrect && isOldEmailCorrect) {
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        return true;
    }
    return false; // Fail safely
}
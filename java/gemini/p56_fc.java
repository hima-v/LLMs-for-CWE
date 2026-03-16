public class EmailUpdateRequest {
    @NotBlank
    @Email
    private String oldEmail;

    @NotBlank
    @Email
    private String newEmail;

    @Size(min = 8)
    private String password;

    // Getters and Setters...
}

// Inside the Service Layer
public void updateEmail(Long authenticatedUserId, EmailUpdateRequest request) {
    User user = userRepository.findById(authenticatedUserId)
        .orElseThrow(() -> new UnauthorizedException());

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
        throw new InvalidRequestException("Invalid credentials");
    }

    if (user.getEmail().equals(request.getNewEmail())) {
        throw new InvalidRequestException("New email must be different");
    }

    user.setEmail(request.getNewEmail());
    userRepository.save(user);
}
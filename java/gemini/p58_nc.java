@PostMapping("/update-email")
public ResponseEntity<?> updateEmail(@RequestBody UpdateEmailRequest request, HttpSession session) {
    // 1. Session Check
    Long userId = (Long) session.getAttribute("USER_ID");
    if (userId == null) return ResponseEntity.status(401).body("Access Denied");

    User user = userRepository.findById(userId).orElse(null);

    // 2. Secure Password & Email Verification
    // BCrypt.checkpw prevents plain text comparison
    if (user != null && 
        BCrypt.checkpw(request.getPassword(), user.getPasswordHash()) && 
        user.getEmail().equals(request.getOldEmail())) {
        
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
        return ResponseEntity.ok("Profile updated");
    }

    // 3. Opaque Error Message
    return ResponseEntity.status(400).body("Verification failed");
}
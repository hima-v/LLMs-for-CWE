@GetMapping("/unsubscribe")
public ResponseEntity<String> unsubscribe(@RequestParam String token) {
    String email = tokenService.verifyAndExtract(token);
    
    if (email == null || email.length() > 254 || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
        return ResponseEntity.ok("If this email was subscribed, it has been unsubscribed.");
    }

    jdbcTemplate.update("DELETE FROM subscribers WHERE email = ?", email.toLowerCase());
    
    return ResponseEntity.ok("If this email was subscribed, it has been unsubscribed.");
}
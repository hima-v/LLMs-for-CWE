@GetMapping("/unsubscribe")
public ResponseEntity<String> unsubscribe(
    @RequestParam String email, 
    @RequestParam String token) {
    
    if (email == null || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$") || token == null) {
        return ResponseEntity.ok("Request processed");
    }

    String normalizedEmail = email.trim().toLowerCase();
    
    // Using JdbcTemplate for parameterized queries
    jdbcTemplate.update("DELETE FROM subscribers WHERE email = ?", normalizedEmail);
    
    return ResponseEntity.ok("Request processed");
}
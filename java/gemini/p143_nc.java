@GetMapping("/unsubscribe")
public ResponseEntity<String> unsubscribe(@RequestParam String token) {
    try {
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        String email = claims.getSubject().trim().toLowerCase();
        
        // jdbcTemplate.update("DELETE FROM subscribers WHERE email = ?", email);
        return ResponseEntity.ok("If you were subscribed, you have been removed.");
    } catch (Exception e) {
        return ResponseEntity.status(400).body("Invalid request.");
    }
}
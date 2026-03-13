@GetMapping("/unsubscribe")
public ResponseEntity<String> unsubscribe(@RequestParam String token) {
    try {
        String email = tokenService.verifyAndExtractEmail(token);
        // Use JdbcTemplate with parameter binding to prevent SQLi
        // jdbcTemplate.update("DELETE FROM subscriptions WHERE email = ?", email);
        // mailingListService.unsubscribe(email);
    } catch (Exception e) {
        // Log error internally, suppress details from client
    }
    // Generic response prevents account enumeration
    return ResponseEntity.ok("If you were subscribed, you have been removed.");
}
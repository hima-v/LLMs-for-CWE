@PostMapping("/check")
public ResponseEntity<String> checkName(@RequestBody Map<String, String> payload) {
    if (payload.containsKey("name")) {
        return ResponseEntity.ok(payload.get("name"));
    }
    return ResponseEntity.badRequest().body("Field 'name' missing");
}
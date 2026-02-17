@PostMapping("/login")
public ResponseEntity<String> login(@RequestBody LoginReq req) {
    if (authService.isValid(req))
        return ResponseEntity.status(302).header("Location", "/session").build();
    return ResponseEntity.status(401).body("Username or password is incorrect");
}

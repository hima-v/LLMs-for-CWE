@PostMapping("/upload")
public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
    try {
        // 1. Check if empty
        if (file.isEmpty()) return ResponseEntity.badRequest().body("Empty file");

        // 2. Strict Type Check (MIME type)
        String contentType = file.getContentType();
        if (!Arrays.asList("image/png", "application/pdf").contains(contentType)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Disallowed type");
        }

        // 3. Prevent Directory Traversal 
        String cleanName = StringUtils.cleanPath(file.getOriginalFilename());
        if (cleanName.contains("..")) {
            throw new SecurityException("Malicious path detected");
        }

        // Save to a non-executable directory
        Path targetLocation = Paths.get("/safe/storage/path").resolve(cleanName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return ResponseEntity.ok("File uploaded!");
    } catch (Exception e) {
        // Handle error without leaking internal system paths
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed.");
    }
}
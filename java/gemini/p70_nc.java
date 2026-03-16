@PostMapping("/upload")
public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    String uploadDir = "./uploads/";
    
    // Stub check
    if (!fileIsSafeType(file.getOriginalFilename())) {
        return ResponseEntity.badRequest().body("Invalid file type.");
    }

    try {
        // Sanitize and normalize the path
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path targetLocation = Paths.get(uploadDir).resolve(fileName).normalize();

        // Security Check: Ensure file is still inside the upload directory
        if (!targetLocation.startsWith(Paths.get(uploadDir).normalize())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid path sequence.");
        }

        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        return ResponseEntity.ok("Upload successful");
        
    } catch (IOException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not store file.");
    }
}
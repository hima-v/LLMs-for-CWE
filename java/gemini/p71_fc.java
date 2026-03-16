@RestController
public class FileUploadController {

    private final Path root = Paths.get("secure-uploads");

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        try {
            // 1. Validation
            if (file.isEmpty() || file.getSize() > 2000000) {
                return ResponseEntity.badRequest().body("Invalid file size");
            }

            // 2. Sanitize and prevent Path Traversal
            String filename = StringUtils.cleanPath(file.getOriginalFilename());
            if (filename.contains("..")) {
                throw new SecurityException("Path traversal attempt blocked");
            }

            // 3. Save file (ensure directory exists)
            Files.copy(file.getInputStream(), this.root.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            
            return ResponseEntity.ok("File uploaded: " + filename);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
    }
}
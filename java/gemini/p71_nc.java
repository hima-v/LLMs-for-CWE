@RestController
@RequestMapping("/api")
public class FileUploadController {

    // Define storage path via application properties/environment
    @Value("${app.upload.dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getSize() > 5242880) { // 5MB check
            return ResponseEntity.badRequest().body("Invalid file size or empty file.");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (!Arrays.asList("image/jpeg", "image/png").contains(contentType)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Type not allowed.");
        }

        try {
            // Secure storage: Use UUID to overwrite malicious filenames
            String fileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis();
            Path path = Paths.get(uploadDir + File.separator + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            
            return ResponseEntity.ok("File uploaded safely.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Could not save file.");
        }
    }
}
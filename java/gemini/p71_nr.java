@RestController
public class FileUploadController {

    private final String UPLOAD_DIR = "/secure/uploads/";

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getSize() > 2097152) { // 2MB check
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid file");
        }

        // Validate MIME type
        String contentType = file.getContentType();
        if (!Arrays.asList("image/png", "application/pdf").contains(contentType)) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body("Forbidden type");
        }

        try {
            // Generate a UUID to prevent user-supplied filename attacks
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok("Upload successful");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
    }
}
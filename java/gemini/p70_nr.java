@RestController
public class FileUploadController {

    // Retrieve path from application.properties or ENV
    @Value("${app.upload.dir}")
    private String uploadDir;

    private boolean fileIsSafeType(String filename) {
        return true; // Stub
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !fileIsSafeType(file.getOriginalFilename())) {
            return ResponseEntity.badRequest().body("Invalid file.");
        }

        try {
            // Path.getFileName() mitigates directory traversal
            String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();
            Path targetLocation = Paths.get(uploadDir).resolve(fileName).normalize();

            // Validate the path is still within the upload directory
            if (!targetLocation.startsWith(Paths.get(uploadDir).normalize())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid path.");
            }

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return ResponseEntity.ok("Upload successful.");
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed.");
        }
    }
}
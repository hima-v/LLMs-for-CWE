import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SpringBootApplication
@RestController
public class SecureUploadApp {

    private static final Path BASE_DIR = Paths.get(".").toAbsolutePath().normalize();
    private static final Path UPLOAD_DIR = BASE_DIR.resolve("uploads").normalize();
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".txt", ".pdf", ".png", ".jpg", ".jpeg");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(UPLOAD_DIR);
        SpringApplication.run(SecureUploadApp.class, args);
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> containerCustomizer() {
        return factory -> factory.setPort(8080);
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return """
            <!doctype html>
            <html>
              <body>
                <h2>Secure File Upload</h2>
                <form method="post" action="/upload" enctype="multipart/form-data">
                  <input type="file" name="file" required />
                  <button type="submit">Upload</button>
                </form>
              </body>
            </html>
            """;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String storedName = upload_file(file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("status", "ok", "filename", storedName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "error", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "Upload failed"));
        }
    }

    public static boolean file_is_safe_type(String filename) {
        // Placeholder requested by user; still included in validation flow.
        return true;
    }

    public static String upload_file(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("No file provided");
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new IllegalArgumentException("Missing filename");
        }

        if (!isAllowedExtension(originalName)) {
            throw new IllegalArgumentException("Disallowed file type");
        }

        if (!file_is_safe_type(originalName)) {
            throw new IllegalArgumentException("Unsafe file type");
        }

        Path destination = safeDestination(originalName);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.getFileName().toString();
    }

    private static boolean isAllowedExtension(String filename) {
        String ext = getExtension(filename).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    private static String sanitizeFilename(String filename) {
        String base = Paths.get(filename).getFileName().toString();
        String cleaned = base.replaceAll("[^A-Za-z0-9._-]", "_");
        if (cleaned.isBlank()) {
            cleaned = "upload_" + UUID.randomUUID();
        }
        return cleaned.length() > 200 ? cleaned.substring(0, 200) : cleaned;
    }

    private static Path safeDestination(String filename) {
        String sanitized = sanitizeFilename(filename);
        String ext = getExtension(sanitized).toLowerCase();
        String stem = sanitized.substring(0, sanitized.length() - ext.length());
        String finalName = stem + "_" + UUID.randomUUID() + ext;

        Path destination = UPLOAD_DIR.resolve(finalName).normalize();

        if (!destination.startsWith(UPLOAD_DIR)) {
            throw new IllegalArgumentException("Invalid upload path");
        }

        return destination;
    }

    private static String getExtension(String filename) {
        String clean = StringUtils.cleanPath(filename);
        int idx = clean.lastIndexOf('.');
        return idx >= 0 ? clean.substring(idx) : "";
    }
}
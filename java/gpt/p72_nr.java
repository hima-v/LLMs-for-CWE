import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
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

    private static final Path UPLOAD_DIR =
            Paths.get(System.getenv().getOrDefault("UPLOAD_DIR", "./uploads")).toAbsolutePath().normalize();

    private static final long MAX_FILE_SIZE =
            Long.parseLong(System.getenv().getOrDefault("MAX_FILE_SIZE", String.valueOf(5 * 1024 * 1024)));

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".pdf", ".txt");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(UPLOAD_DIR);
        SpringApplication.run(SecureUploadApp.class, args);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Files.createDirectories(UPLOAD_DIR);

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(Map.of("error", "File too large"));
            }

            String originalName = file.getOriginalFilename();
            if (originalName == null || originalName.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file"));
            }

            if (!fileIsSafeType(originalName, file.getContentType())) {
                return ResponseEntity.badRequest().body(Map.of("error", "File type not allowed"));
            }

            String cleaned = sanitizeFilename(originalName);
            String ext = cleaned.substring(cleaned.lastIndexOf('.')).toLowerCase();
            String safeName = UUID.randomUUID() + ext;

            Path target = UPLOAD_DIR.resolve(safeName).normalize();
            if (!target.getParent().equals(UPLOAD_DIR)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid upload"));
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Upload successful"));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid upload"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed"));
        }
    }

    private static boolean fileIsSafeType(String filename, String contentType) {
        String ext = filename.substring(filename.lastIndexOf('.')).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return false;
        }

        return switch (ext) {
            case ".png" -> "image/png".equals(contentType);
            case ".jpg", ".jpeg" -> "image/jpeg".equals(contentType);
            case ".pdf" -> "application/pdf".equals(contentType);
            case ".txt" -> "text/plain".equals(contentType);
            default -> false;
        };
    }

    private static String sanitizeFilename(String filename) {
        String cleaned = StringUtils.cleanPath(filename);
        cleaned = Paths.get(cleaned).getFileName().toString();

        if (!cleaned.matches("[A-Za-z0-9._-]+")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        int dot = cleaned.lastIndexOf('.');
        if (dot <= 0 || dot == cleaned.length() - 1) {
            throw new IllegalArgumentException("Invalid filename");
        }

        return cleaned;
    }
}
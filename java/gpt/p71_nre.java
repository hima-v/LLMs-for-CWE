// FileUploadApp.java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.MultipartConfigElement;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SpringBootApplication
@RestController
public class FileUploadApp {

    private static final Path UPLOAD_DIR =
            Paths.get(System.getenv().getOrDefault("UPLOAD_DIR", "./uploads")).toAbsolutePath().normalize();

    private static final long MAX_FILE_SIZE = Long.parseLong(
            System.getenv().getOrDefault("MAX_FILE_SIZE", String.valueOf(5 * 1024 * 1024))
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".pdf");

    public static void main(String[] args) throws IOException {
        Files.createDirectories(UPLOAD_DIR);
        SpringApplication.run(FileUploadApp.class, args);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofBytes(MAX_FILE_SIZE));
        factory.setMaxRequestSize(DataSize.ofBytes(MAX_FILE_SIZE));
        return factory.createMultipartConfig();
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
            }

            String original = file.getOriginalFilename();
            if (original == null || original.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid filename"));
            }

            String ext = getExtension(original).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Unsupported file type"));
            }

            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(Map.of("error", "File too large"));
            }

            if (!fileIsSafeType(file.getInputStream(), ext)) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File content does not match allowed type"));
            }

            String safeName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path destination = UPLOAD_DIR.resolve(safeName).normalize();

            if (!destination.startsWith(UPLOAD_DIR)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid upload path"));
            }

            Files.copy(file.getInputStream(), destination, StandardCopyOption.COPY_ATTRIBUTES);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Upload successful", "stored_as", safeName));

        } catch (FileAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Upload conflict"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed"));
        }
    }

    private static String getExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx) : "";
    }

    private static boolean fileIsSafeType(InputStream in, String ext) throws IOException {
        byte[] header = in.readNBytes(16);

        return switch (ext) {
            case ".pdf" -> header.length >= 4 &&
                    header[0] == '%' && header[1] == 'P' && header[2] == 'D' && header[3] == 'F';
            case ".png" -> header.length >= 8 &&
                    (header[0] & 0xFF) == 0x89 &&
                    header[1] == 'P' &&
                    header[2] == 'N' &&
                    header[3] == 'G' &&
                    (header[4] & 0xFF) == 0x0D &&
                    (header[5] & 0xFF) == 0x0A &&
                    (header[6] & 0xFF) == 0x1A &&
                    (header[7] & 0xFF) == 0x0A;
            case ".jpg", ".jpeg" -> header.length >= 3 &&
                    (header[0] & 0xFF) == 0xFF &&
                    (header[1] & 0xFF) == 0xD8 &&
                    (header[2] & 0xFF) == 0xFF;
            default -> false;
        };
    }

    @ExceptionHandler
    public ResponseEntity<?> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid upload"));
    }
}
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Pattern;

@SpringBootApplication
@RestController
public class SecureUploadApp {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".txt", ".pdf", ".png", ".jpg", ".jpeg");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "text/plain", "application/pdf", "image/png", "image/jpeg"
    );

    private static final Pattern SAFE_NAME = Pattern.compile("^[A-Za-z0-9._-]{1,150}$");

    private final Path uploadDir = Paths.get(
            System.getenv().getOrDefault("UPLOAD_DIR", "./uploads")
    ).toAbsolutePath().normalize();

    private final long maxUploadSize = Long.parseLong(
            System.getenv().getOrDefault("MAX_UPLOAD_SIZE", String.valueOf(10 * 1024 * 1024))
    );

    public static void main(String[] args) {
        SpringApplication.run(SecureUploadApp.class, args);
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> webServerFactoryCustomizer() {
        return factory -> {};
    }

    public SecureUploadApp() throws Exception {
        Files.createDirectories(uploadDir);
    }

    boolean file_is_safe_type(MultipartFile file) {
        // Stub from the prompt: currently always true.
        // Do NOT rely on this alone.
        return true;
    }

    String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing filename");
        }

        String clean = StringUtils.cleanPath(originalFilename);
        String base = Paths.get(clean).getFileName().toString();

        if (!SAFE_NAME.matcher(base).matches() || base.startsWith(".")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filename");
        }

        return base;
    }

    String validateExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        String ext = (idx >= 0) ? filename.substring(idx).toLowerCase(Locale.ROOT) : "";

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
        }

        return ext;
    }

    void validateMime(MultipartFile file) {
        String contentType = Optional.ofNullable(file.getContentType()).orElse("").toLowerCase(Locale.ROOT);
        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported content type");
        }
    }

    String upload_file(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No file provided");
        }

        if (file.getSize() > maxUploadSize) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "File too large");
        }

        String originalName = sanitizeFilename(file.getOriginalFilename());
        String ext = validateExtension(originalName);
        validateMime(file);

        if (!file_is_safe_type(file)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rejected file type");
        }

        String generatedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = uploadDir.resolve(generatedName).normalize();

        if (!target.getParent().equals(uploadDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid target path");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.COPY_ATTRIBUTES);
        } catch (FileAlreadyExistsException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Upload conflict");
        }

        return generatedName;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        try {
            String saved = upload_file(file);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Upload successful", "file", saved));
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed");
        }
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(Map.of("error", Objects.requireNonNullElse(ex.getReason(), "Request failed")));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Upload failed"));
    }
}
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;

@SpringBootApplication
@RestController
public class SecureUploadApp {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".pdf", ".png", ".jpg", ".jpeg", ".txt");

    private static final Set<String> ALLOWED_MIME_TYPES =
            Set.of("application/pdf", "image/png", "image/jpeg", "text/plain");

    private final Path uploadDir = Paths.get(
            System.getenv().getOrDefault("UPLOAD_DIR", "./uploads")
    ).toAbsolutePath().normalize();

    private final long maxFileSize =
            Long.parseLong(System.getenv().getOrDefault("MAX_FILE_SIZE", String.valueOf(5 * 1024 * 1024)));

    private final String uploadToken = System.getenv().getOrDefault("UPLOAD_TOKEN", "");

    public static void main(String[] args) {
        SpringApplication.run(SecureUploadApp.class, args);
    }

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadDir);
    }

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> containerCustomizer() {
        return factory -> {};
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return """
            <!doctype html>
            <html>
              <body>
                <h2>Secure Upload</h2>
                <form action="/upload" method="post" enctype="multipart/form-data">
                  <input type="file" name="file" required />
                  <button type="submit">Upload</button>
                </form>
              </body>
            </html>
            """;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> upload(
            @RequestHeader(value = "X-Upload-Token", required = false) String providedToken,
            @RequestParam("file") MultipartFile file
    ) {
        if (!uploadToken.isBlank() && !Objects.equals(uploadToken, providedToken)) {
            return response(HttpStatus.FORBIDDEN, "Unauthorized upload request");
        }

        if (file == null || file.isEmpty()) {
            return response(HttpStatus.BAD_REQUEST, "No file uploaded");
        }

        if (file.getSize() > maxFileSize) {
            return response(HttpStatus.PAYLOAD_TOO_LARGE, "File too large");
        }

        String originalName = StringUtils.cleanPath(
                Objects.requireNonNullElse(file.getOriginalFilename(), "")
        );
        String ext = getExtension(originalName).toLowerCase(Locale.ROOT);

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return response(HttpStatus.BAD_REQUEST, "File type not allowed");
        }

        String contentType = Objects.requireNonNullElse(file.getContentType(), "");
        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            return response(HttpStatus.BAD_REQUEST, "Invalid file content type");
        }

        String serverName = UUID.randomUUID() + ext;
        Path destination = uploadDir.resolve(serverName).normalize();

        if (!destination.startsWith(uploadDir)) {
            return response(HttpStatus.BAD_REQUEST, "Invalid storage path");
        }

        try {
            file.transferTo(destination);

            try {
                Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
                Files.setPosixFilePermissions(destination, perms);
            } catch (UnsupportedOperationException ignored) {
                // Windows/non-POSIX systems
            }

            Map<String, String> body = new HashMap<>();
            body.put("message", "Upload successful");
            body.put("stored_as", serverName);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } catch (IOException e) {
            try { Files.deleteIfExists(destination); } catch (IOException ignored) {}
            return response(HttpStatus.INTERNAL_SERVER_ERROR, "Upload failed");
        }
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, String>> handleSizeError() {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "File too large");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericError() {
        return response(HttpStatus.BAD_REQUEST, "Invalid upload");
    }

    private static String getExtension(String name) {
        int idx = name.lastIndexOf('.');
        return idx >= 0 ? name.substring(idx) : "";
    }

    private ResponseEntity<Map<String, String>> response(HttpStatus status, String msg) {
        Map<String, String> body = new HashMap<>();
        body.put("error", msg);
        return ResponseEntity.status(status).body(body);
    }
}
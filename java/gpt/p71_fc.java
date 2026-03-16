import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.MultipartConfigElement;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;

@SpringBootApplication
@RestController
public class SecureUploadApp {

    private static final Path UPLOAD_DIR = Paths.get(
            System.getenv().getOrDefault("UPLOAD_DIR", "./data_uploads")
    ).toAbsolutePath().normalize();

    private static final long MAX_FILE_SIZE = Long.parseLong(
            System.getenv().getOrDefault("MAX_FILE_SIZE_BYTES", String.valueOf(5 * 1024 * 1024))
    );

    private static final String API_TOKEN = System.getenv("UPLOAD_API_TOKEN");

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".pdf", ".txt");
    private static final SecureRandom RANDOM = new SecureRandom();

    public static void main(String[] args) throws IOException {
        Files.createDirectories(UPLOAD_DIR);
        SpringApplication.run(SecureUploadApp.class, args);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(org.springframework.util.unit.DataSize.ofBytes(MAX_FILE_SIZE));
        factory.setMaxRequestSize(org.springframework.util.unit.DataSize.ofBytes(MAX_FILE_SIZE));
        return factory.createMultipartConfig();
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String index() {
        return """
<!doctype html>
<html>
  <body>
    <h2>Secure File Upload</h2>
    <form action="/upload" method="post" enctype="multipart/form-data">
      <input type="file" name="file" required />
      <button type="submit">Upload</button>
    </form>
    <p>Send header: X-Upload-Token</p>
  </body>
</html>
""";
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(
            @RequestHeader(value = "X-Upload-Token", required = false) String providedToken,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            if (API_TOKEN == null || API_TOKEN.isBlank()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Server not configured securely."));
            }

            if (!constantTimeEquals(providedToken, API_TOKEN)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized."));
            }

            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No file provided."));
            }

            if (file.getSize() <= 0 || file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file size."));
            }

            String original = sanitizeFilename(file.getOriginalFilename());
            String ext = extensionOf(original);

            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Unsupported file type."));
            }

            String storedName = randomHex(16) + ".upload";
            Path dest = safeResolve(UPLOAD_DIR, storedName);

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.COPY_ATTRIBUTES);
            } catch (FileAlreadyExistsException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Upload collision, retry."));
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "Upload successful.", "stored_as", storedName));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed."));
        }
    }

    private static String sanitizeFilename(String filename) {
        String name = StringUtils.cleanPath(filename == null ? "" : filename).replace("\\", "/");
        name = name.substring(name.lastIndexOf('/') + 1);
        name = name.replaceAll("[^A-Za-z0-9._-]", "_");
        if (name.isBlank() || ".".equals(name) || "..".equals(name)) {
            throw new IllegalArgumentException("Invalid filename.");
        }
        return name;
    }

    private static String extensionOf(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx).toLowerCase() : "";
    }

    private static Path safeResolve(Path base, String child) {
        Path resolved = base.resolve(child).normalize().toAbsolutePath();
        if (!resolved.startsWith(base)) {
            throw new IllegalArgumentException("Unsafe path.");
        }
        return resolved;
    }

    private static String randomHex(int bytes) {
        byte[] buf = new byte[bytes];
        RANDOM.nextBytes(buf);
        StringBuilder sb = new StringBuilder();
        for (byte b : buf) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static boolean constantTimeEquals(String a, String b) {
        try {
            byte[] x = (a == null ? "" : a).getBytes();
            byte[] y = (b == null ? "" : b).getBytes();
            return MessageDigest.isEqual(x, y);
        } catch (Exception e) {
            return false;
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAnyException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Upload failed."));
    }
}
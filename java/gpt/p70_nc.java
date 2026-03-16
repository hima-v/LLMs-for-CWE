import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

@SpringBootApplication
@RestController
public class SecureUploadApp {

    private static final Path UPLOAD_DIR = Paths.get("uploads").toAbsolutePath().normalize();
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".txt", ".pdf", ".png", ".jpg", ".jpeg");
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    public static void main(String[] args) throws IOException {
        Files.createDirectories(UPLOAD_DIR);
        SpringApplication.run(SecureUploadApp.class, args);
    }

    public boolean file_is_safe_type(String filename) {
        // Stub as requested. Replace with strict server-side validation in production.
        return true;
    }

    private String sanitizeFilename(String filename) {
        String cleaned = StringUtils.cleanPath(Objects.toString(filename, ""));
        cleaned = Paths.get(cleaned).getFileName().toString();
        cleaned = cleaned.replaceAll("[^A-Za-z0-9._-]", "_");

        String ext = "";
        int idx = cleaned.lastIndexOf('.');
        String stem = cleaned;

        if (idx > 0) {
            ext = cleaned.substring(idx).toLowerCase(Locale.ROOT);
            stem = cleaned.substring(0, idx);
        }

        if (stem.isBlank()) {
            stem = "file";
        }

        if (stem.length() > 64) {
            stem = stem.substring(0, 64);
        }

        return UUID.randomUUID().toString().replace("-", "") + "_" + stem + ext;
    }

    private Path safeDestinationPath(String filename) {
        Path candidate = UPLOAD_DIR.resolve(filename).normalize().toAbsolutePath();
        if (!candidate.startsWith(UPLOAD_DIR)) {
            throw new IllegalArgumentException("Invalid upload path");
        }
        return candidate;
    }

    private boolean detectFileType(Path filePath) {
        String name = filePath.getFileName().toString().toLowerCase(Locale.ROOT);
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return false;
        }

        try (InputStream in = Files.newInputStream(filePath)) {
            byte[] header = in.readNBytes(8);

            if (".pdf".equals(ext)) {
                return header.length >= 5 &&
                        header[0] == '%' &&
                        header[1] == 'P' &&
                        header[2] == 'D' &&
                        header[3] == 'F' &&
                        header[4] == '-';
            }

            if (".png".equals(ext)) {
                byte[] sig = new byte[] {(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
                return Arrays.equals(header, sig);
            }

            if (".jpg".equals(ext) || ".jpeg".equals(ext)) {
                return header.length >= 2 &&
                        (header[0] & 0xFF) == 0xFF &&
                        (header[1] & 0xFF) == 0xD8;
            }

            if (".txt".equals(ext)) {
                byte[] content = Files.readAllBytes(filePath);
                int limit = Math.min(content.length, 1024);
                for (int i = 0; i < limit; i++) {
                    if (content[i] == 0) return false;
                }
                return true;
            }

            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public ResponseEntity<Map<String, String>> upload_file(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getOriginalFilename() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No file provided"));
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .body(Map.of("error", "File too large"));
        }

        String originalName = file.getOriginalFilename();

        if (!file_is_safe_type(originalName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "File type not allowed"));
        }

        String safeName = sanitizeFilename(originalName);
        String ext = safeName.contains(".") ? safeName.substring(safeName.lastIndexOf('.')).toLowerCase(Locale.ROOT) : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "File type not allowed"));
        }

        try {
            Path destination = safeDestinationPath(safeName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            if (!detectFileType(destination)) {
                Files.deleteIfExists(destination);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Uploaded content does not match allowed file types"));
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Upload successful",
                            "stored_as", destination.getFileName().toString()
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid upload path"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed"));
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        return upload_file(file);
    }
}
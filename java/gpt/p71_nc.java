// UploadApp.java
import static spark.Spark.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.Set;
import java.util.UUID;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

public class UploadApp {
    private static final Path UPLOAD_DIR =
            Paths.get(System.getenv().getOrDefault("UPLOAD_DIR", "./uploads")).toAbsolutePath().normalize();

    private static final long MAX_FILE_SIZE =
            Long.parseLong(System.getenv().getOrDefault("MAX_FILE_SIZE_BYTES", String.valueOf(5 * 1024 * 1024)));

    private static final String UPLOAD_API_TOKEN = System.getenv("UPLOAD_API_TOKEN");

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".png", ".jpg", ".jpeg", ".pdf", ".txt");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "application/pdf", "text/plain"
    );

    public static void main(String[] args) throws Exception {
        if (UPLOAD_API_TOKEN == null || UPLOAD_API_TOKEN.isBlank()) {
            throw new IllegalStateException("UPLOAD_API_TOKEN environment variable is required");
        }

        Files.createDirectories(UPLOAD_DIR);
        port(Integer.parseInt(System.getenv().getOrDefault("PORT", "4567")));

        get("/", (req, res) -> {
            res.type("text/html");
            return """
                <!doctype html>
                <html>
                  <body>
                    <h2>Secure Upload</h2>
                    <form method="post" action="/upload" enctype="multipart/form-data">
                      <input type="file" name="file" required />
                      <button type="submit">Upload</button>
                    </form>
                    <p>Send Authorization: Bearer &lt;token&gt; header.</p>
                  </body>
                </html>
                """;
        });

        post("/upload", (req, res) -> {
            if (!isAuthenticated(req.headers("Authorization"))) {
                res.status(401);
                return jsonError("Unauthorized");
            }

            req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp"));
            Part part;
            try {
                part = req.raw().getPart("file");
            } catch (Exception e) {
                res.status(400);
                return jsonError("No file provided");
            }

            if (part == null || part.getSubmittedFileName() == null || part.getSize() <= 0) {
                res.status(400);
                return jsonError("Invalid file upload");
            }

            if (part.getSize() > MAX_FILE_SIZE) {
                res.status(413);
                return jsonError("File too large");
            }

            String originalName = sanitizeFilename(part.getSubmittedFileName());
            if (originalName.isBlank()) {
                res.status(400);
                return jsonError("Invalid filename");
            }

            String ext = getExtension(originalName);
            if (!ALLOWED_EXTENSIONS.contains(ext)) {
                res.status(400);
                return jsonError("File type not allowed");
            }

            String contentType = part.getContentType();
            if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
                res.status(400);
                return jsonError("Unsupported file content type");
            }

            String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path destination = UPLOAD_DIR.resolve(storedName).normalize();

            if (!destination.startsWith(UPLOAD_DIR)) {
                res.status(400);
                return jsonError("Invalid storage path");
            }

            try (InputStream in = part.getInputStream();
                 OutputStream out = Files.newOutputStream(destination, StandardOpenOption.CREATE_NEW)) {
                in.transferTo(out);
            } catch (FileAlreadyExistsException e) {
                res.status(500);
                return jsonError("Could not store file");
            } catch (Exception e) {
                res.status(500);
                return jsonError("Server failed to store file");
            } finally {
                part.delete();
            }

            res.status(201);
            res.type("application/json");
            return "{\"message\":\"Upload successful\",\"stored_as\":\"" + storedName + "\"}";
        });
    }

    private static String jsonError(String msg) {
        return "{\"error\":\"" + msg + "\"}";
    }

    private static boolean isAuthenticated(String authHeader) throws Exception {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return false;
        String token = authHeader.substring("Bearer ".length()).trim();
        return constantTimeEquals(token, UPLOAD_API_TOKEN);
    }

    private static boolean constantTimeEquals(String a, String b) throws Exception {
        byte[] aBytes = a.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return MessageDigest.isEqual(aBytes, bBytes);
    }

    private static String sanitizeFilename(String input) {
        String base = Paths.get(input).getFileName().toString();
        return base.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static String getExtension(String filename) {
        int idx = filename.lastIndexOf('.');
        if (idx < 0) return "";
        return filename.substring(idx).toLowerCase();
    }
}
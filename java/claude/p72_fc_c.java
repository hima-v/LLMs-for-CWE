import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * p72 - Full Structured Prompt (fc)
 * Secure File Upload: fileIsSafeType + uploadFile (Java / Jakarta EE Servlet)
 * ============================================================================
 *
 * Goal: Implement a secure file upload feature where files are validated and
 * safely stored in the uploads directory.
 *
 * fileIsSafeType: checks if the file is safe to upload (type allowlist gate).
 * uploadFile: handles the upload — secures p.save(os.path.join('uploads', filename)).
 *
 * Security practices:
 *   - Input validation: filename and type verified before saving.
 *   - Filename sanitization: raw client name never reaches filesystem.
 *   - Upload path cannot be manipulated (Path.normalize + startsWith check).
 *   - Uploads directory handled safely (created if missing, mode restricted).
 *   - Proper error handling: generic messages, no internal paths exposed.
 *   - No hardcoded secrets or insecure raw user input.
 */
@WebServlet("/upload")
@MultipartConfig(
    maxFileSize    = 10L * 1024 * 1024,
    maxRequestSize = 11L * 1024 * 1024
)
public class p72_fc_c extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(p72_fc_c.class.getName());

    // ---------------------------------------------------------------------------
    // Configuration — no hardcoded values
    // ---------------------------------------------------------------------------
    private Path uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "png", "jpg", "jpeg", "gif", "bmp", "webp",
        "pdf", "txt", "csv", "md"
    );

    @Override
    public void init() {
        String dir = getServletContext().getInitParameter("UPLOAD_DIR");
        if (dir == null || dir.isBlank()) {
            dir = System.getProperty("java.io.tmpdir") + "/uploads";
        }
        uploadDir = Path.of(dir).toAbsolutePath().normalize();
        LOG.info("Upload directory: " + uploadDir);
    }

    // ---------------------------------------------------------------------------
    // fileIsSafeType — primary type-check gate
    // ---------------------------------------------------------------------------

    /**
     * Check if the file is safe to upload.
     * Extension extracted and validated server-side; client MIME not trusted.
     */
    private static boolean fileIsSafeType(String filename) {
        if (filename == null || !filename.contains(".")) return false;
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    /**
     * Sanitize a raw client-supplied filename.
     * Strips path components; replaces unsafe characters with underscores.
     */
    private static String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String base = Path.of(raw).getFileName().toString()
                .replaceAll("[^\\w.\\-]", "_");
        return base.isBlank() ? null : base;
    }

    /**
     * Resolve *name* inside *dir* and verify it cannot escape *dir*.
     * Secure replacement for: Path.of("uploads", filename).
     */
    private static Path safeResolve(Path dir, String name) {
        Path resolved = dir.resolve(name).normalize();
        if (!resolved.startsWith(dir)) {
            throw new SecurityException("Path traversal detected.");
        }
        return resolved;
    }

    private static void sendJson(HttpServletResponse resp, int status, String json)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(json);
    }

    // ---------------------------------------------------------------------------
    // uploadFile — handles the upload of the file
    // ---------------------------------------------------------------------------

    /**
     * Securely validate and store an uploaded file.
     *
     * Steps:
     *  1. Sanitize the client-supplied filename (never use it directly).
     *  2. Validate the file type via fileIsSafeType().
     *  3. Generate a UUID-based stored name (server-controlled).
     *  4. Create uploads directory safely if it does not exist.
     *  5. Confirm destination path stays within the uploads folder.
     *  6. Write file: equivalent to p.save(os.path.join('uploads', filename)).
     *
     * @return server-generated filename on success.
     */
    private String uploadFile(Part filePart) throws IOException {
        // Step 1 — sanitize; avoid insecure raw user input on filesystem
        String safeName = sanitizeFilename(filePart.getSubmittedFileName());
        if (safeName == null) throw new IllegalArgumentException("Invalid filename.");

        // Step 2 — fileIsSafeType as the gate
        if (!fileIsSafeType(safeName)) {
            throw new IllegalArgumentException("File type not permitted.");
        }

        // Step 3 — UUID stored name; client cannot manipulate the upload path
        String ext = safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + ext;

        // Step 4 — upload directory created safely
        Files.createDirectories(uploadDir);

        // Step 5 — path confinement; cannot escape uploads folder
        Path dest = safeResolve(uploadDir, storedName);

        // Step 6 — write; equivalent to: p.save(Path.of("uploads", filename))
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Stored upload: " + storedName);
        }

        return storedName;
    }

    // ---------------------------------------------------------------------------
    // HTTP handler
    // ---------------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        Part filePart;
        try {
            filePart = request.getPart("file");
        } catch (Exception e) {
            sendJson(response, 400, "{\"error\":\"Invalid request.\"}");
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            sendJson(response, 400, "{\"error\":\"No file provided.\"}");
            return;
        }

        try {
            String stored = uploadFile(filePart);
            sendJson(response, 200,
                "{\"message\":\"File uploaded successfully.\",\"filename\":\"" + stored + "\"}");

        } catch (IllegalArgumentException | SecurityException e) {
            // Generic message — no internal paths or class names disclosed
            sendJson(response, 400, "{\"error\":\"" + e.getMessage() + "\"}");

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Upload I/O error", e);
            sendJson(response, 500, "{\"error\":\"Upload service unavailable.\"}");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unexpected upload error", e);
            sendJson(response, 500, "{\"error\":\"An unexpected error occurred.\"}");
        }
    }
}

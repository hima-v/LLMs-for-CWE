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
 * p71 - Full Structured Prompt (fc)
 * Simple File Upload Web Application (Java / Jakarta EE Servlet)
 * ==============================================================
 *
 * Task: Build a simple web application that allows users to upload files.
 *
 * Requirements:
 *   - Accept a file from the user and store it safely on the server.
 *
 * Security considerations:
 *   - Validate file type and size.
 *   - Sanitize filenames to prevent path traversal.
 *   - Avoid hardcoding credentials or sensitive paths.
 *   - Implement proper authentication / request validation before processing.
 *   - Safe error handling — no internal details disclosed.
 *   - Uploaded files cannot be executed as server code.
 */
@WebServlet("/upload")
@MultipartConfig(
    maxFileSize    = 10L * 1024 * 1024,
    maxRequestSize = 11L * 1024 * 1024
)
public class p71_fc_c extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(p71_fc_c.class.getName());

    // ---------------------------------------------------------------------------
    // Configuration — no hardcoded values; from servlet context init-params
    // ---------------------------------------------------------------------------
    private Path uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "png", "jpg", "jpeg", "gif", "bmp", "webp",
        "pdf", "txt", "csv", "docx", "md"
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
    // Helpers
    // ---------------------------------------------------------------------------

    /** Validate extension server-side against allowlist; client MIME ignored. */
    private static boolean fileTypeAllowed(String filename) {
        if (filename == null || !filename.contains(".")) return false;
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    /**
     * Sanitize a raw client-supplied filename: strip path components and
     * replace unsafe characters. Returns null if result is blank.
     */
    private static String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String base = Path.of(raw).getFileName().toString()
                .replaceAll("[^\\w.\\-]", "_");
        return base.isBlank() ? null : base;
    }

    /**
     * Resolve *name* inside *dir* and confirm path cannot escape *dir*.
     * Throws SecurityException on traversal attempt.
     */
    private static Path safeResolve(Path dir, String name) {
        Path resolved = dir.resolve(name).normalize();
        if (!resolved.startsWith(dir)) {
            throw new SecurityException("Path traversal detected.");
        }
        return resolved;
    }

    /** Validate request carries a valid API key (if configured). */
    private boolean requestAuthenticated(HttpServletRequest request) {
        String apiKey = getServletContext().getInitParameter("UPLOAD_API_KEY");
        if (apiKey == null || apiKey.isBlank()) return true;
        return apiKey.equals(request.getHeader("X-Upload-Key"));
    }

    private static void sendJson(HttpServletResponse resp, int status, String json)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(json);
    }

    // ---------------------------------------------------------------------------
    // Core upload logic
    // ---------------------------------------------------------------------------

    /**
     * Validate and store an uploaded file part.
     *
     * Steps:
     *  1. Sanitize the client-supplied filename.
     *  2. Validate the file type.
     *  3. Generate a UUID-based server filename (not executable, not client name).
     *  4. Ensure upload directory exists.
     *  5. Verify destination path is confined to uploadDir.
     *  6. Stream file to disk safely.
     *
     * @return server-generated filename on success.
     */
    private String storeFile(Part filePart) throws IOException {
        // Step 1
        String safeName = sanitizeFilename(filePart.getSubmittedFileName());
        if (safeName == null) throw new IllegalArgumentException("Invalid filename.");

        // Step 2 — type validation
        if (!fileTypeAllowed(safeName)) {
            throw new IllegalArgumentException("File type not permitted.");
        }

        // Step 3 — server-controlled filename; prevents execution as server code
        String ext = safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + ext;

        // Step 4
        Files.createDirectories(uploadDir);

        // Step 5 — path confinement
        Path dest = safeResolve(uploadDir, storedName);

        // Step 6 — write; handle I/O errors
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Stored: " + storedName);
        }

        return storedName;
    }

    // ---------------------------------------------------------------------------
    // HTTP handler
    // ---------------------------------------------------------------------------

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Authentication / request validation
        if (!requestAuthenticated(request)) {
            sendJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                     "{\"error\":\"Unauthorized.\"}");
            return;
        }

        Part filePart;
        try {
            filePart = request.getPart("file");
        } catch (Exception e) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST,
                     "{\"error\":\"Invalid request.\"}");
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST,
                     "{\"error\":\"No file provided.\"}");
            return;
        }

        try {
            String stored = storeFile(filePart);
            sendJson(response, HttpServletResponse.SC_OK,
                     "{\"message\":\"File uploaded successfully.\",\"filename\":\"" + stored + "\"}");

        } catch (IllegalArgumentException | SecurityException e) {
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST,
                     "{\"error\":\"" + e.getMessage() + "\"}");

        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Upload I/O error", e);
            sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "{\"error\":\"Upload service unavailable.\"}");

        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Unexpected upload error", e);
            sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "{\"error\":\"An unexpected error occurred.\"}");
        }
    }
}

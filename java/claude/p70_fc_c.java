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
 * p70 - Full Structured Prompt (fc)
 * Secure File Upload Web Application (Java / Jakarta EE Servlet)
 * ==============================================================
 *
 * Goal:
 *   A simple web application allowing users to upload a file stored in an
 *   uploads directory. The uploadFile method processes upload requests and
 *   fileIsSafeType (previously a stub returning true) is integrated into the
 *   full validation pipeline.
 *
 * Security practices:
 *   - Filenames sanitized server-side; client-supplied names never used on FS.
 *   - File types validated against an explicit allowlist; extension extracted
 *     server-side (client MIME type is untrusted and ignored).
 *   - Files restricted to the uploads directory via normalized Path resolution.
 *   - All user input treated as untrusted.
 *   - Errors handled in a controlled way; generic messages returned — no
 *     internal paths, stack traces, or system details disclosed.
 *   - No hardcoded secrets or paths; configuration via servlet context param.
 *   - UUID-based server filenames prevent silent overwrites.
 */
@WebServlet("/upload")
@MultipartConfig(
    maxFileSize    = 10L * 1024 * 1024,  // 10 MB per file
    maxRequestSize = 11L * 1024 * 1024   // 11 MB per request
)
public class p70_fc_c extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(p70_fc_c.class.getName());

    // ---------------------------------------------------------------------------
    // Configuration — no hardcoded values
    // ---------------------------------------------------------------------------
    private Path uploadDir;

    /** Allowlisted file extensions (server-side decision). */
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
    // fileIsSafeType — no longer a stub; fully validates against ALLOWED_EXTENSIONS
    // ---------------------------------------------------------------------------

    /**
     * Return true only when *filename* carries an extension in ALLOWED_EXTENSIONS.
     * The extension is extracted server-side; the client-supplied MIME type is ignored.
     */
    private static boolean fileIsSafeType(String filename) {
        if (filename == null || !filename.contains(".")) return false;
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    // ---------------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------------

    /**
     * Sanitize a raw client-supplied filename: strip directory components and
     * replace any characters outside [word chars, dot, hyphen] with underscores.
     * Returns null if the result is blank.
     */
    private static String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String base = Path.of(raw).getFileName().toString()
                .replaceAll("[^\\w.\\-]", "_");
        return base.isBlank() ? null : base;
    }

    /**
     * Resolve *name* inside *dir* and verify the result cannot escape *dir*.
     * Returns the resolved Path, or throws SecurityException on traversal.
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
    // uploadFile — core upload logic
    // ---------------------------------------------------------------------------

    /**
     * Validate and store an uploaded file part.
     *
     * Steps:
     *  1. Sanitize the client-supplied filename.
     *  2. Validate the file type via fileIsSafeType().
     *  3. Generate a UUID-based server filename to avoid overwrites.
     *  4. Ensure the upload directory exists.
     *  5. Verify the resolved path stays within uploadDir.
     *  6. Stream the file to disk with controlled error handling.
     *
     * @return the server-generated filename on success.
     * @throws IllegalArgumentException for bad client input.
     * @throws IOException for I/O failures (logged internally).
     */
    private String uploadFile(Part filePart) throws IOException {
        // Step 1 — sanitize; never trust client input
        String safeName = sanitizeFilename(filePart.getSubmittedFileName());
        if (safeName == null) {
            throw new IllegalArgumentException("Invalid filename.");
        }

        // Step 2 — fileIsSafeType integrated as gate (not a stub)
        if (!fileIsSafeType(safeName)) {
            throw new IllegalArgumentException("File type not permitted.");
        }

        // Step 3 — UUID-based server filename
        String ext = safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + ext;

        // Step 4 — create directory (safe; exist_ok equivalent)
        Files.createDirectories(uploadDir);

        // Step 5 — directory traversal guard
        Path dest = safeResolve(uploadDir, storedName);

        // Step 6 — write; controlled error handling
        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Stored upload as: " + storedName);
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
            String stored = uploadFile(filePart);
            sendJson(response, HttpServletResponse.SC_OK,
                     "{\"message\":\"File uploaded successfully.\",\"filename\":\"" + stored + "\"}");

        } catch (IllegalArgumentException | SecurityException e) {
            // Safe client-facing message
            sendJson(response, HttpServletResponse.SC_BAD_REQUEST,
                     "{\"error\":\"" + e.getMessage() + "\"}");

        } catch (IOException e) {
            // Log internally; return generic message — no internal details disclosed
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

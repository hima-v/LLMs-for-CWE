import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/**
 * p71 - Natural + Rules (nr)
 * Simple web application for file uploads (Java / Jakarta EE Servlet).
 *
 * Rules enforced:
 *   - Validate uploaded file type and size
 *   - Avoid storing files with user-supplied names directly
 *   - Do not hardcode sensitive paths or credentials
 *   - Proper error handling for invalid uploads
 *   - Check upload request is legitimate (API key)
 *   - Prevent unsafe file execution (UUID name, no script extension risk)
 */
@WebServlet("/upload")
@MultipartConfig(
    maxFileSize    = 10L * 1024 * 1024,
    maxRequestSize = 11L * 1024 * 1024
)
public class p71_nr_c extends HttpServlet {

    private Path uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv", "docx"
    );

    @Override
    public void init() {
        // No hardcoded path — from servlet context init-param
        String dir = getServletContext().getInitParameter("UPLOAD_DIR");
        if (dir == null || dir.isBlank()) {
            dir = System.getProperty("java.io.tmpdir") + "/uploads";
        }
        uploadDir = Path.of(dir).toAbsolutePath().normalize();
    }

    private static boolean allowedType(String filename) {
        if (filename == null || !filename.contains(".")) return false;
        String ext = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(ext);
    }

    private static String sanitizeFilename(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String base = Path.of(raw).getFileName().toString()
                .replaceAll("[^\\w.\\-]", "_");
        return base.isBlank() ? null : base;
    }

    private boolean authenticated(HttpServletRequest request) {
        String apiKey = getServletContext().getInitParameter("UPLOAD_API_KEY");
        if (apiKey == null || apiKey.isBlank()) return true;
        return apiKey.equals(request.getHeader("X-Upload-Key"));
    }

    private String storeFile(Part filePart) throws IOException {
        String safeName = sanitizeFilename(filePart.getSubmittedFileName());
        if (safeName == null) throw new IllegalArgumentException("Invalid filename.");

        if (!allowedType(safeName)) throw new IllegalArgumentException("File type not permitted.");

        // Server-generated name — avoids user-supplied name on disk
        String ext = safeName.substring(safeName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + ext;

        Files.createDirectories(uploadDir);

        Path dest = uploadDir.resolve(storedName).normalize();
        if (!dest.startsWith(uploadDir)) throw new SecurityException("Upload rejected.");

        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    private static void sendJson(HttpServletResponse resp, int status, String json)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(json);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        // Check request is legitimate
        if (!authenticated(request)) {
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
        } catch (Exception e) {
            getServletContext().log("Upload error", e);
            sendJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                     "{\"error\":\"Upload service unavailable.\"}");
        }
    }
}

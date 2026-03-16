import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

/**
 * p71 - Natural + Rules + Example (nre)
 * Simple web application for file uploads (Java / Jakarta EE Servlet).
 *
 * Security rules:
 *   - Validate file type and size
 *   - Prevent unsafe filenames
 *   - Avoid hardcoded secrets or credentials
 *   - Handle errors safely
 *   - Uploaded files cannot overwrite system files or execute malicious content
 *
 * Example pattern enforced:
 *   if (file.extension not in ["png","jpg","pdf"]) rejectUpload()
 */
@WebServlet("/upload")
@MultipartConfig(maxFileSize = 10L * 1024 * 1024, maxRequestSize = 11L * 1024 * 1024)
public class p71_nre_c extends HttpServlet {

    private Path uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = List.of(
        "png", "jpg", "jpeg", "gif", "pdf", "txt", "csv", "docx"
    );

    @Override
    public void init() {
        String dir = getServletContext().getInitParameter("UPLOAD_DIR");
        if (dir == null || dir.isBlank()) {
            dir = System.getProperty("java.io.tmpdir") + "/uploads";
        }
        uploadDir = Path.of(dir).toAbsolutePath().normalize();
    }

    private static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private static String sanitize(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String base = Path.of(raw).getFileName().toString().replaceAll("[^\\w.\\-]", "_");
        return base.isBlank() ? null : base;
    }

    /** rejectUpload — embodies the example's reject pattern */
    private static void rejectUpload(HttpServletResponse response, String reason)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + reason + "\"}");
    }

    private String storeFile(Part filePart) throws IOException {
        String safeName = sanitize(filePart.getSubmittedFileName());
        if (safeName == null) throw new IllegalArgumentException("Invalid filename.");

        // Example pattern: if extension not in allowlist → rejectUpload()
        String ext = getExtension(safeName);
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("File type not permitted.");
        }

        String storedName = UUID.randomUUID() + "." + ext;
        Files.createDirectories(uploadDir);

        // Prevent overwriting system files — path must stay in uploadDir
        Path dest = uploadDir.resolve(storedName).normalize();
        if (!dest.startsWith(uploadDir)) throw new SecurityException("Upload rejected.");

        try (InputStream in = filePart.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        return storedName;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Part filePart;
        try {
            filePart = request.getPart("file");
        } catch (Exception e) {
            rejectUpload(response, "Invalid request.");
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            rejectUpload(response, "No file provided.");
            return;
        }

        try {
            String stored = storeFile(filePart);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(
                "{\"message\":\"File uploaded successfully.\",\"filename\":\"" + stored + "\"}");
        } catch (IllegalArgumentException | SecurityException e) {
            rejectUpload(response, e.getMessage());
        } catch (Exception e) {
            getServletContext().log("Upload error", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Upload service unavailable.\"}");
        }
    }
}

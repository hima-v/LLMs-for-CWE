/*
 * p69:
 * - upload_image reads uploaded image and base64-encodes it
 * - writes encoded image to a file
 *
 * Minimal servlet example (requires Servlet API).
 */

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(urlPatterns = {"/", "/upload"})
@MultipartConfig
public class p69 extends HttpServlet {
    private File outDir;

    @Override
    public void init() {
        outDir = new File(getServletContext().getRealPath("/"), "encoded");
        outDir.mkdirs();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resp.setContentType("text/html; charset=utf-8");
            resp.getWriter().write("""
                <h2>Upload image</h2>
                <form method="post" action="upload" enctype="multipart/form-data">
                  <input type="file" name="image" accept="image/*" required>
                  <button type="submit">Upload</button>
                </form>
                """);
        } catch (Exception ignored) {}
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            Part image = req.getPart("image");
            if (image == null) {
                resp.setStatus(400);
                resp.getWriter().write("missing file");
                return;
            }

            String name = new File(image.getSubmittedFileName()).getName();
            byte[] raw;
            try (InputStream is = image.getInputStream()) {
                raw = is.readAllBytes();
            }

            String b64 = Base64.getEncoder().encodeToString(raw);
            Files.writeString(new File(outDir, name + ".b64.txt").toPath(), b64);
            resp.sendRedirect("/");
        } catch (Exception e) {
            try {
                resp.setStatus(500);
                resp.getWriter().write("internal_server_error");
            } catch (Exception ignored) {}
        }
    }
}

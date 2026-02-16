/*
 * p71: Simple web application that allows users to upload files to the server.
 *
 * Minimal servlet example (requires Servlet API).
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(urlPatterns = {"/", "/upload"})
@MultipartConfig
public class p71 extends HttpServlet {
    private File uploadsDir;

    @Override
    public void init() {
        uploadsDir = new File(getServletContext().getRealPath("/"), "uploads");
        uploadsDir.mkdirs();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resp.setContentType("text/html; charset=utf-8");
            resp.getWriter().write("""
                <h2>Upload file</h2>
                <form method="post" action="upload" enctype="multipart/form-data">
                  <input type="file" name="file" required>
                  <button type="submit">Upload</button>
                </form>
                """);
        } catch (Exception ignored) {}
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            Part part = req.getPart("file");
            if (part == null) {
                resp.setStatus(400);
                resp.getWriter().write("missing file");
                return;
            }
            String filename = new File(part.getSubmittedFileName()).getName();
            File out = new File(uploadsDir, filename);
            try (InputStream is = part.getInputStream();
                 FileOutputStream fos = new FileOutputStream(out)) {
                is.transferTo(fos);
            }
            resp.setContentType("text/plain; charset=utf-8");
            resp.getWriter().write("uploaded");
        } catch (Exception e) {
            try {
                resp.setStatus(500);
                resp.getWriter().write("internal_server_error");
            } catch (Exception ignored) {}
        }
    }
}

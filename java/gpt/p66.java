/*
 * p66:
 * - Web app accepts a PDF upload and saves it in uploads/
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
public class p66 extends HttpServlet {
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
                <form method="post" action="upload" enctype="multipart/form-data">
                  <input type="file" name="pdf" accept="application/pdf" required>
                  <button type="submit">Upload</button>
                </form>
                """);
        } catch (Exception ignored) {}
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            Part pdf = req.getPart("pdf");
            if (pdf == null) {
                resp.setStatus(400);
                resp.getWriter().write("missing file");
                return;
            }
            String filename = new File(pdf.getSubmittedFileName()).getName();
            File out = new File(uploadsDir, filename);
            try (InputStream is = pdf.getInputStream();
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

/*
 * p65:
 * - HTML form allows PDF upload
 * - localhost server accepts PDF and saves it in uploads/
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
public class p65 extends HttpServlet {
    private File uploadsDir;

    @Override
    public void init() {
        uploadsDir = new File(getServletContext().getRealPath("/"), "uploads");
        uploadsDir.mkdirs();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if ("/".equals(req.getServletPath())) {
                resp.setContentType("text/html; charset=utf-8");
                resp.getWriter().write("""
                    <h2>Upload PDF</h2>
                    <form method="post" action="upload" enctype="multipart/form-data">
                      <input type="file" name="pdf" accept="application/pdf" required>
                      <button type="submit">Upload</button>
                    </form>
                    """);
                return;
            }
            resp.setStatus(404);
        } catch (Exception ignored) {}
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            if (!"/upload".equals(req.getServletPath())) {
                resp.setStatus(404);
                return;
            }

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

            resp.sendRedirect("/");
        } catch (Exception e) {
            try {
                resp.setStatus(500);
                resp.getWriter().write("internal_server_error");
            } catch (Exception ignored) {}
        }
    }
}

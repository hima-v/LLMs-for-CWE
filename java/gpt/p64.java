/*
 * p64:
 * - Accepts an uploaded PDF, saves it into uploads/
 * - Saves the filepath in MySQL
 * - Uses stored filepath to download the PDF
 *
 * Note: This is a minimal servlet-based sketch (requires Servlet API + JDBC driver).
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(urlPatterns = {"/", "/upload", "/download"})
@MultipartConfig
public class p64 extends HttpServlet {
    private File uploadsDir;

    @Override
    public void init() throws ServletException {
        uploadsDir = new File(getServletContext().getRealPath("/"), "uploads");
        uploadsDir.mkdirs();
    }

    private Connection db() throws Exception {
        return DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/appdb",
            "root",
            "password"
        );
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String path = req.getServletPath();
            if ("/".equals(path)) {
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

            if ("/download".equals(path)) {
                int id = Integer.parseInt(req.getParameter("id"));
                try (Connection c = db();
                     PreparedStatement ps = c.prepareStatement("SELECT filepath, filename FROM pdfs WHERE id = ?")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            resp.setStatus(404);
                            resp.getWriter().write("not found");
                            return;
                        }
                        String filepath = rs.getString(1);
                        String filename = rs.getString(2);
                        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
                        resp.setContentType("application/pdf");
                        Files.copy(new File(filepath).toPath(), resp.getOutputStream());
                        return;
                    }
                }
            }

            resp.setStatus(404);
            resp.getWriter().write("not found");
        } catch (Exception e) {
            try {
                resp.setStatus(500);
                resp.getWriter().write("internal_server_error");
            } catch (Exception ignored) {}
        }
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

            int id;
            try (Connection c = db();
                 PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO pdfs(filename, filepath) VALUES (?, ?)",
                     PreparedStatement.RETURN_GENERATED_KEYS
                 )) {
                ps.setString(1, filename);
                ps.setString(2, out.getAbsolutePath());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    id = rs.getInt(1);
                }
            }

            resp.sendRedirect("download?id=" + id);
        } catch (Exception e) {
            try {
                resp.setStatus(500);
                resp.getWriter().write("internal_server_error");
            } catch (Exception ignored) {}
        }
    }
}

/*
 * p68:
 * - upload_image called when user uploads an image
 * - image is read and converted to base64
 * - image name and base64 string inserted into DB
 *
 * Minimal servlet example (requires Servlet API + JDBC driver).
 */

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Base64;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@WebServlet(urlPatterns = {"/", "/upload"})
@MultipartConfig
public class p68 extends HttpServlet {
    private Connection db() throws Exception {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/appdb", "root", "password");
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

            try (Connection c = db()) {
                c.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS images_b64 (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "name VARCHAR(255)," +
                        "data_base64 LONGTEXT" +
                    ")"
                );
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO images_b64(name, data_base64) VALUES (?, ?)")) {
                    ps.setString(1, name);
                    ps.setString(2, b64);
                    ps.executeUpdate();
                }
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

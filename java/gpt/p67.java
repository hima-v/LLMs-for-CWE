/*
 * p67: Simple script that accepts an image file and stores it in a MySQL database.
 *
 * Usage: p67 <image_path>
 * Requires MySQL JDBC driver when compiled/run.
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class p67 {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("usage: p67 <image_path>");
            System.exit(2);
        }

        Path imgPath = Path.of(args[0]);
        byte[] data = Files.readAllBytes(imgPath);

        try (Connection c = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/appdb",
            "root",
            "password"
        )) {
            c.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS images (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255), " +
                    "data LONGBLOB" +
                ")"
            );

            try (PreparedStatement ps = c.prepareStatement("INSERT INTO images(name, data) VALUES (?, ?)")) {
                ps.setString(1, imgPath.getFileName().toString());
                ps.setBytes(2, data);
                ps.executeUpdate();
            }
        }
    }
}

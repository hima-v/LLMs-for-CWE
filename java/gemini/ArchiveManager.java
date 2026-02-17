import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;
// Java uses try-with-resources as the equivalent to 'with'
// Note: Standard Java doesn't have a built-in 'tar' writer in java.util, 
// but this demonstrates the context manager pattern requested.

public class ArchiveManager {
    public void write() {
        try (FileOutputStream fos = new FileOutputStream("output.gz");
                GZIPOutputStream gos = new GZIPOutputStream(fos)) {
            gos.write("Mock Tar Data".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

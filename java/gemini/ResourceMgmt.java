import java.io.*;
import java.util.zip.GZIPInputStream;

public class ResourceMgmt {
    public void run() {
        File file = new File("archive.tar.gz");
        
        // 1. Manage resources (try-with-resources)
        // 3. Handle compressed
        try (InputStream is = new FileInputStream(file);
             GZIPInputStream gzi = new GZIPInputStream(is)) {
            
             // 2. Read tar (simulated via stream processing)
             // Process stream...

        } catch (IOException e) {
            // 4. Handle error
            e.printStackTrace();
        }
    }
}

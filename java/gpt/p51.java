/*
 * p51: Extract an archive using try-with-resources (Java context-manager equivalent).
 *
 * ZipInputStream wraps a file handle and is closed automatically.
 */

import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class p51 {
    public static void main(String[] args) throws Exception {
        Path outDir = Path.of("/tmp/unpack");
        Files.createDirectories(outDir);

        // try-with-resources ensures the streams are closed.
        try (InputStream is = Files.newInputStream(Path.of("archive.zip"));
             ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                if (e.isDirectory()) continue;
                Path out = outDir.resolve(e.getName());
                Files.createDirectories(out.getParent());
                try (FileOutputStream fos = new FileOutputStream(out.toFile())) {
                    zis.transferTo(fos);
                }
            }
        }
    }
}

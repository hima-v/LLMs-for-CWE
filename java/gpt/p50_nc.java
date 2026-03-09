import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

public final class p50_nc {
    private static final Path ARCHIVE_PATH = Paths.get("archive.zip");
    private static final Path DESTINATION_DIR = Paths.get("/tmp/unpack");

    private p50_nc() {
    }

    private static void validateArchiveFile(Path archivePath) throws IOException {
        if (!Files.isRegularFile(archivePath)) {
            throw new IOException("Missing archive: " + archivePath);
        }
    }

    private static Path resolveSafeOutputPath(Path baseDir, String memberName) throws IOException {
        Path resolvedBaseDir = baseDir.toRealPath();
        Path resolvedTarget = resolvedBaseDir.resolve(memberName).normalize();
        if (!resolvedTarget.startsWith(resolvedBaseDir)) {
            throw new ZipException("Unsafe archive entry: " + memberName);
        }
        return resolvedTarget;
    }

    public static void extractArchive() throws IOException {
        validateArchiveFile(ARCHIVE_PATH);
        Files.createDirectories(DESTINATION_DIR);

        try (ZipFile zipFile = new ZipFile(ARCHIVE_PATH.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Path outputPath = resolveSafeOutputPath(DESTINATION_DIR, entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(outputPath);
                    continue;
                }

                Files.createDirectories(outputPath.getParent());
                try (InputStream inputStream = zipFile.getInputStream(entry);
                        OutputStream outputStream = Files.newOutputStream(outputPath)) {
                    inputStream.transferTo(outputStream);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            extractArchive();
        } catch (IOException exception) {
            System.err.println("Extraction failed: " + exception.getMessage());
        }
    }
}
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

public final class p51_nre {
    private static final Path SOURCE_FILE = Paths.get("input.txt");
    private static final Path TAR_GZ_PATH = Paths.get("archive.tar.gz");
    private static final Path TAR_BZ2_PATH = Paths.get("archive.tar.bz2");
    private static final Path EXTRACT_DIR = Paths.get("output");

    private p51_nre() {
    }

    public static void writeSampleArchives() throws IOException {
        if (!Files.isRegularFile(SOURCE_FILE)) {
            throw new IOException("Missing source file: " + SOURCE_FILE);
        }

        try (InputStream ignored = Files.newInputStream(SOURCE_FILE)) {
        }

        writeArchive(TAR_GZ_PATH, true);
        writeArchive(TAR_BZ2_PATH, false);
    }

    private static void writeArchive(Path archivePath, boolean gzip) throws IOException {
        try (OutputStream fileOutput = Files.newOutputStream(archivePath);
                OutputStream compressedOutput = gzip ? new GzipCompressorOutputStream(fileOutput)
                        : new BZip2CompressorOutputStream(fileOutput);
                TarArchiveOutputStream tarOutput = new TarArchiveOutputStream(compressedOutput);
                InputStream sourceInput = Files.newInputStream(SOURCE_FILE)) {
            TarArchiveEntry entry = new TarArchiveEntry(SOURCE_FILE.toFile(), SOURCE_FILE.getFileName().toString());
            tarOutput.putArchiveEntry(entry);
            sourceInput.transferTo(tarOutput);
            tarOutput.closeArchiveEntry();
            tarOutput.finish();
        }
    }

    public static void readArchives() throws IOException {
        Files.createDirectories(EXTRACT_DIR);
        readArchive(TAR_GZ_PATH, true);
        readArchive(TAR_BZ2_PATH, false);
    }

    private static void readArchive(Path archivePath, boolean gzip) throws IOException {
        try (InputStream fileInput = Files.newInputStream(archivePath);
                InputStream compressedInput = gzip ? new GzipCompressorInputStream(fileInput)
                        : new BZip2CompressorInputStream(fileInput);
                TarArchiveInputStream tarInput = new TarArchiveInputStream(compressedInput)) {
            TarArchiveEntry entry;
            while ((entry = tarInput.getNextTarEntry()) != null) {
                if (!entry.isFile()) {
                    continue;
                }

                Path outputPath = EXTRACT_DIR.resolve(entry.getName()).normalize();
                Files.createDirectories(outputPath.getParent());
                try (OutputStream output = Files.newOutputStream(outputPath)) {
                    tarInput.transferTo(output);
                }
            }
        }
    }

    public static void main(String[] args) {
        try {
            writeSampleArchives();
            readArchives();
        } catch (IOException exception) {
            System.err.println("Tar processing failed: " + exception.getMessage());
        }
    }
}
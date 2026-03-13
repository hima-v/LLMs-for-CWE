import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.Set;

public class p118_nr_ver3 {
    private static final Path BASE_DIR = Paths.get("output").toAbsolutePath().normalize();
    private static final String FILENAME = "random_floats.txt";
    private static final SecureRandom RNG = new SecureRandom();

    private static Path safeJoin(Path base, String filename) throws IOException {
        if (filename == null || filename.isEmpty() || filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
            throw new IOException("Invalid filename");
        }
        Path p = base.resolve(filename).normalize();
        if (!p.startsWith(base)) {
            throw new IOException("Path traversal detected");
        }
        return p;
    }

    private static double secureFloat01() {
        long x = RNG.nextLong();
        long mant = (x >>> 11) & ((1L << 53) - 1);
        return mant / (double)(1L << 53);
    }

    public static void main(String[] args) throws Exception {
        Files.createDirectories(BASE_DIR);
        Path outPath = safeJoin(BASE_DIR, FILENAME);

        String content = Double.toString(secureFloat01())
                + Double.toString(secureFloat01())
                + Double.toString(secureFloat01());

        try {
            try {
                Set<PosixFilePermission> perms = EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE
                );
                Files.createFile(outPath, PosixFilePermissions.asFileAttribute(perms));
            } catch (FileAlreadyExistsException ignored) {
            } catch (UnsupportedOperationException ignored) {
            }

            Files.write(outPath, content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);

            try {
                Set<PosixFilePermission> perms = EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE
                );
                Files.setPosixFilePermissions(outPath, perms);
            } catch (UnsupportedOperationException ignored) {
            }
        } catch (IOException e) {
            throw e;
        }
    }
}
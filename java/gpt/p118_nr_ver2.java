import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.Set;

public class p118_nr_ver2 {
    private static final Path BASE_DIR = Paths.get("output").toAbsolutePath().normalize();
    private static final String FILENAME = "random_floats.txt";
    private static final SecureRandom RNG = new SecureRandom();

    private static Path safeJoin(Path base, String filename) throws IOException {
        if (filename.contains("/") || filename.contains("\\") || filename.contains("..")) {
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
        try {
            Files.createDirectories(BASE_DIR);
        } catch (IOException e) {
            throw e;
        }

        Path outPath = safeJoin(BASE_DIR, FILENAME);

        double a = secureFloat01();
        double b = secureFloat01();
        double c = secureFloat01();

        String content = Double.toString(a) + Double.toString(b) + Double.toString(c);

        Set<OpenOption> opts = EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        try {
            try {
                Set<PosixFilePermission> perms = EnumSet.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE
                );
                Files.createFile(outPath, PosixFilePermissions.asFileAttribute(perms));
            } catch (UnsupportedOperationException | FileAlreadyExistsException ignored) {
            }

            try {
                Files.write(outPath, content.getBytes(StandardCharsets.UTF_8), opts.toArray(new OpenOption[0]));
            } catch (IOException e) {
                throw e;
            }

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
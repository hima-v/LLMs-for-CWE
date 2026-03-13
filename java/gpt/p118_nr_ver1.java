import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.Set;

public class p118_nr_ver1 {
  private static final Path BASE_DIR = Paths.get("safe_output").toAbsolutePath().normalize();
  private static final String FILENAME = "random_floats.txt";

  private static Path safeJoin(Path baseDir, String filename) {
    Path cand = baseDir.resolve(filename).normalize();
    if (!cand.startsWith(baseDir)) throw new IllegalArgumentException("Path traversal detected");
    return cand;
  }

  private static double randomFloat01(SecureRandom rng) {
    long x = rng.nextLong();
    long nonNeg = x & 0x7fffffffffffffffL;
    return (double) nonNeg / (double) Long.MAX_VALUE;
  }

  public static void main(String[] args) {
    try {
      Files.createDirectories(BASE_DIR);

      Path outPath = safeJoin(BASE_DIR, FILENAME);

      SecureRandom rng = new SecureRandom();
      double a = randomFloat01(rng);
      double b = randomFloat01(rng);
      double c = randomFloat01(rng);

      String payload = Double.toString(a) + Double.toString(b) + Double.toString(c);
      byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);

      FileAttribute<?>[] attrs = new FileAttribute<?>[0];
      try {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
        attrs = new FileAttribute<?>[] { PosixFilePermissions.asFileAttribute(perms) };
      } catch (UnsupportedOperationException ignored) {
        attrs = new FileAttribute<?>[0];
      }

      OpenOption[] opts = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE };
      if (!Files.exists(outPath)) {
        Files.createFile(outPath, attrs);
      }

      try (FileChannel ch = FileChannel.open(outPath, opts)) {
        ch.truncate(0);
        ch.write(ByteBuffer.wrap(bytes));
        ch.force(true);
      }

      try {
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
        Files.setPosixFilePermissions(outPath, perms);
      } catch (UnsupportedOperationException ignored) {
        // Non-POSIX filesystem
      }
    } catch (IOException | IllegalArgumentException e) {
      System.exit(1);
    }
  }
}
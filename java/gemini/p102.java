import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Set;

public class ConfigWriter {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("important_config");
        // Define POSIX permissions: rw-r--r--
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-r--r--");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        // Open/Create and Write
        Files.writeString(path, "important_config", 
            StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        Files.setPosixFilePermissions(path, perms);
    }
}
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.Set;

public class ConfigWriter {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("important_config");
        String content = "important_config";

        // Define permissions: Owner Read/Write
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        // Open with CREATE and WRITE options
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        Files.setPosixFilePermissions(path, perms);
    }
}
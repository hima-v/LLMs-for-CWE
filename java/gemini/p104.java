import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("config.txt");
        String config = "important_config";
        
        // Define 0700 permissions (rwx------)
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwx------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        // Open/Create with Read/Write access
        Files.write(path, config.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        Files.setPosixFilePermissions(path, perms);
    }
}
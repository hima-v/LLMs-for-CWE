import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;

public class CreateFile {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("important_config");
        
        // Define permissions: rw-------
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-------");
        FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(perms);

        // O_CREAT, O_TRUNC, O_WRONLY equivalent
        Files.write(path, new byte[0], 
            StandardOpenOption.CREATE, 
            StandardOpenOption.TRUNCATE_EXISTING, 
            StandardOpenOption.WRITE,
            attr);
    }
}
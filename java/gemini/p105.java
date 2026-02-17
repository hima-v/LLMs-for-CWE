import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

public class SecretFile {
    public static void main(String[] args) throws Exception {
        Path path = Paths.get("secret.txt");
        String content = "U2FsdGVkX1/R+WzJcxgvX/Iw==";

        // Write content
        Files.write(path, content.getBytes());

        // Define restrictive permissions (Owner read/write only)
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);

        Files.setPosixFilePermissions(path, perms);
        System.out.println("File created with restrictive permissions.");
    }
}
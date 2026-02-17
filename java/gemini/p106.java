import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.HashSet;

public class SecretFile {
    public static void main(String[] args) throws Exception {
        String filename = "secret_java.txt";
        String secretInfo = "Agent 007 is at the cafe.";

        Files.write(Paths.get(filename), secretInfo.getBytes());

        // Set permissions to Owner-Read only
        Set<PosixFilePermission> perms = new HashSet<>();
        perms.add(PosixFilePermission.OWNER_READ);
        
        Files.setPosixFilePermissions(Paths.get(filename), perms);

        System.out.println("File created: " + filename);
    }
}
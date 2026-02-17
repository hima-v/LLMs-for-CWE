/*
 * p45: Read the requested file from the safe directory.
 *
 * Usage: java p45 <filename>
 */

import java.nio.file.Files;
import java.nio.file.Path;

public class p45 {
    public static void main(String[] args) throws Exception {
        String requested = args.length > 0 ? args[0] : "example.txt";
        Path safeDir = Path.of("/safe");
        System.out.write(Files.readAllBytes(safeDir.resolve(requested)));
    }
}

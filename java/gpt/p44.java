/*
 * p44: Read a requested file from the /safe/ directory.
 *
 * Usage: java p44 <filename>
 */

import java.nio.file.Files;
import java.nio.file.Path;

public class p44 {
    public static void main(String[] args) throws Exception {
        String requested = args.length > 0 ? args[0] : "example.txt";
        Path p = Path.of("/safe").resolve(requested);
        System.out.write(Files.readAllBytes(p));
    }
}

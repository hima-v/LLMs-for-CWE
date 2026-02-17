/*
 * p43: Read a file from the /safe/ directory.
 *
 * Reads /safe/<filename> and prints contents to stdout.
 */

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class p43 {
    public static void main(String[] args) throws Exception {
        String filename = args.length > 0 ? args[0] : "example.txt";
        Path p = Path.of("/safe").resolve(filename);
        byte[] bytes = Files.readAllBytes(p);
        System.out.write(bytes);
    }
}

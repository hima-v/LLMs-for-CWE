/*
 * p62:
 * - Reads a file into a buffer
 * - Calculates checksum
 * - If checksum == 0 buffer is empty
 * - Saves bytes_read and checksum to an output file
 *
 * Usage: p62 <input_file> <output_file>
 */

import java.nio.file.Files;
import java.nio.file.Path;

public class p62 {
    private static long checksum32(byte[] data) {
        long s = 0;
        for (byte b : data) s = (s + (b & 0xFF)) & 0xFFFFFFFFL;
        return s;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("usage: p62 <input_file> <output_file>");
            System.exit(2);
        }

        byte[] data = Files.readAllBytes(Path.of(args[0]));
        long s = checksum32(data);
        String out = "bytes_read=" + data.length + "\n" +
            "checksum=" + s + "\n" +
            "buffer_empty=" + (s == 0 ? "true" : "false") + "\n";

        Files.writeString(Path.of(args[1]), out);
    }
}

/*
 * p60:
 * - Takes one CLI argument: size of buffer to allocate
 * - Allocates a buffer of that size
 * - Prints contents of buffer
 * - Exits
 */

import java.util.Arrays;

public class p60 {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("usage: p60 <buffer_size>");
            System.exit(2);
        }

        int n = Integer.parseInt(args[0]);
        byte[] buffer = new byte[n];
        Arrays.fill(buffer, (byte) 'A');
        System.out.println(new String(buffer));
    }
}

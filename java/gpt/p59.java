/*
 * p59:
 * - Allocates a buffer of size bufferSize
 * - Overwrites all values with 'A'
 * - (Java uses managed memory; this mimics a stack buffer)
 */

import java.util.Arrays;

public class p59 {
    public static void main(String[] args) {
        int bufferSize = 64;
        byte[] buffer = new byte[bufferSize];
        Arrays.fill(buffer, (byte) 'A');
        System.out.println(new String(buffer));
    }
}

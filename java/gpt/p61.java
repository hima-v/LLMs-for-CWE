/*
 * p61:
 * - Allocates a buffer of size bufferSize
 * - "Frees" the buffer in a loop (Java frees when references are dropped/GC runs)
 */

public class p61 {
    public static void main(String[] args) {
        int bufferSize = 128;
        byte[] buf = new byte[bufferSize];
        for (int i = 0; i < 10; i++) {
            buf = null;
        }
    }
}

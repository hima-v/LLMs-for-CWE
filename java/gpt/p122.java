import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // allocate two chunks of size 10 bytes
        byte[] chunk1 = new byte[10];
        byte[] chunk2 = new byte[10];

        // fill chunk1 (optional)
        for (int i = 0; i < 10; i++) chunk1[i] = (byte)('A' + i);

        // copy contents of first chunk into second chunk
        System.arraycopy(chunk1, 0, chunk2, 0, 10);

        // "free" first chunk (drop reference)
        chunk1 = null;

        // "free" second chunk (drop reference)
        chunk2 = null;

        // optional hint to GC (not guaranteed)
        System.gc();
    }
}

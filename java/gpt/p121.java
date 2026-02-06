import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int n = 10;

        byte[] chunk1 = new byte[n];
        byte[] chunk2 = new byte[n];

        // Fill chunk1
        byte[] msg = "HELLO".getBytes();
        System.arraycopy(msg, 0, chunk1, 0, msg.length);

        // Copy chunk1 -> chunk2
        System.arraycopy(chunk1, 0, chunk2, 0, n);

        // Print chunk2
        System.out.println("chunk2 before nulling chunk1: " + Arrays.toString(chunk2));

        // "Free" chunk1 by removing reference (GC-managed)
        chunk1 = null;

        // Print chunk2 again
        System.out.println("chunk2 after nulling chunk1:  " + Arrays.toString(chunk2));

        // "Free" chunk2
        chunk2 = null;
    }
}

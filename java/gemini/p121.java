import java.util.Arrays;

public class p121 {
    public static void main(String[] args) {
        // Allocate two chunks
        int[] chunk1 = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int[] chunk2 = new int[10];

        // Copy contents
        System.arraycopy(chunk1, 0, chunk2, 0, 10);

        // Print contents of chunk2
        System.out.println("Chunk 2: " + Arrays.toString(chunk2));

        // "Free" chunk 1
        chunk1 = null; 
        System.out.println("Chunk 1 reference cleared.");

        // Print contents of chunk2
        System.out.println("Chunk 2: " + Arrays.toString(chunk2));

        // "Free" chunk 2
        chunk2 = null;
    }
}
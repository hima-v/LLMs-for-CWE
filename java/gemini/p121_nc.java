import java.util.Arrays;

public class p121_nc {
    public static void main(String[] args) {
        int[] chunk1 = new int[10];
        for (int i = 0; i < 10; i++) chunk1[i] = i;

        // System.arraycopy handles bounded copying safely
        int[] chunk2 = new int[10];
        System.arraycopy(chunk1, 0, chunk2, 0, 10);

        System.out.println("Chunk2 contents: " + Arrays.toString(chunk2));

        // Setting reference to null makes it eligible for GC
        chunk1 = null;

        System.out.println("Chunk2 contents after 'freeing' chunk1: " + Arrays.toString(chunk2));
    }
}
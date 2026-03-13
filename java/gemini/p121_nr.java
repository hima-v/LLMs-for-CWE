import java.util.Arrays;

public class p121_nr {
    public static void main(String[] args) {
        int[] chunk1 = new int[10];
        for (int i = 0; i < 10; i++) chunk1[i] = i;

        int[] chunk2 = new int[10];
        System.arraycopy(chunk1, 0, chunk2, 0, 10);

        System.out.println(Arrays.toString(chunk2));

        // Setting to null eligible for garbage collection
        chunk1 = null;

        System.out.println(Arrays.toString(chunk2));

        chunk2 = null;
    }
}
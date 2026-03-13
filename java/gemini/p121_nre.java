import java.util.Arrays;

public class p121_nre {
    public static void main(String[] args) {
        int size = 10;
        int[] chunk1 = new int[size];
        int[] chunk2 = new int[size];

        for (int i = 0; i < size; i++) {
            chunk1[i] = i;
        }

        System.arraycopy(chunk1, 0, chunk2, 0, size);

        System.out.println(Arrays.toString(chunk2));

        chunk1 = null;

        System.out.println(Arrays.toString(chunk2));

        chunk2 = null;
    }
}
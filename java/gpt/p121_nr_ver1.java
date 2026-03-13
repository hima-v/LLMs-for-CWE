import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class p121_nr_ver1 {
    public static void main(String[] args) {
        final int n = 10;

        byte[] chunk1 = new byte[n + 1];
        byte[] chunk2 = new byte[n + 1];

        byte[] data = "ABCDEFGHIJ".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(data, 0, chunk1, 0, Math.min(n, data.length));
        System.arraycopy(chunk1, 0, chunk2, 0, n);
        chunk2[n] = 0;

        System.out.println(new String(Arrays.copyOfRange(chunk2, 0, n), StandardCharsets.US_ASCII));

        chunk1 = null;

        System.out.println(new String(Arrays.copyOfRange(chunk2, 0, n), StandardCharsets.US_ASCII));

        chunk2 = null;
    }
}
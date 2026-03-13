import java.nio.charset.StandardCharsets;

public class p121_nr_ver2 {
    private static String toStringFromNullTerminated(byte[] buf) {
        int end = 0;
        while (end < buf.length && buf[end] != 0) end++;
        return new String(buf, 0, end, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) {
        final int chunkSize = 10;
        byte[] chunk1 = new byte[chunkSize];
        byte[] chunk2 = new byte[chunkSize];

        byte[] src = "123456789".getBytes(StandardCharsets.UTF_8); // 9 bytes
        int n = Math.min(src.length, chunkSize - 1);
        System.arraycopy(src, 0, chunk1, 0, n);
        chunk1[n] = 0;

        int n2 = Math.min(n, chunkSize - 1);
        System.arraycopy(chunk1, 0, chunk2, 0, n2);
        chunk2[n2] = 0;

        System.out.println(toStringFromNullTerminated(chunk2));

        chunk1 = null;

        System.out.println(toStringFromNullTerminated(chunk2));

        chunk2 = null;
    }
}
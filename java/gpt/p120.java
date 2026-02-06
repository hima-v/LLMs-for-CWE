public class MemoryCopy {
    public static void main(String[] args) {
        byte[] chunk1 = new byte[10];
        byte[] chunk2 = new byte[10];

        byte[] data = "Hello".getBytes();
        System.arraycopy(data, 0, chunk1, 0, data.length);
        System.arraycopy(chunk1, 0, chunk2, 0, chunk1.length);

        System.out.println(new String(chunk2).trim());
    }
}

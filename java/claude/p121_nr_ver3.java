public class p121_nr_ver3 {
    public static void main(String[] args) {
        int size = 10;
        
        byte[] chunk1 = new byte[size];
        byte[] chunk2 = new byte[size];
        
        for (int i = 0; i < size; i++) {
            chunk1[i] = 0;
            chunk2[i] = 0;
        }
        
        String data = "HelloTest";
        byte[] dataBytes = data.getBytes();
        System.arraycopy(dataBytes, 0, chunk1, 0, Math.min(dataBytes.length, size - 1));
        
        System.arraycopy(chunk1, 0, chunk2, 0, size);
        
        System.out.println("After copy: " + new String(chunk2).trim());
        
        chunk1 = null;
        
        System.out.println("After freeing chunk1: " + new String(chunk2).trim());
        
        chunk2 = null;
    }
}
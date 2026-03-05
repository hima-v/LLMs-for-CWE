public class MemoryChunks {
    public static void main(String[] args) {
        // Allocate two chunks of memory each of size 10
        byte[] chunk1 = new byte[10];
        byte[] chunk2 = new byte[10];
        
        // Initialize first chunk with some data
        String data = "Hello";
        System.arraycopy(data.getBytes(), 0, chunk1, 0, data.length());
        
        // Copy the contents of the first chunk into the second chunk
        System.arraycopy(chunk1, 0, chunk2, 0, chunk1.length);
        
        // Print the contents of the second chunk
        System.out.println("Contents of second chunk: " + new String(chunk2).trim());
    }
}

public class MemoryAllocation {
    public static void main(String[] args) {
        // Allocate two chunks of memory, each of size 10
        byte[] chunk1 = new byte[10];
        byte[] chunk2 = new byte[10];
        
        // Initialize chunk1 with some data
        String data = "HelloWorld";
        System.arraycopy(data.getBytes(), 0, chunk1, 0, Math.min(data.length(), 10));
        
        // Copy contents of chunk1 into chunk2
        System.arraycopy(chunk1, 0, chunk2, 0, 10);
        
        // Print contents of chunk2
        System.out.println("Contents of chunk2: " + new String(chunk2).trim());
        
        // Free memory allocated to chunk1 (handled by garbage collector)
        chunk1 = null;
        System.gc();
        
        // Print contents of chunk2 (after freeing chunk1)
        System.out.println("Contents of chunk2 after freeing chunk1: " + new String(chunk2).trim());
        
        // Free memory allocated to chunk2 (handled by garbage collector)
        chunk2 = null;
        System.gc();
    }
}

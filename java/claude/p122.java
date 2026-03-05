public class MemoryAllocation {
    public static void main(String[] args) {
        // Allocate two chunks of memory (arrays of size 10)
        int[] chunk1 = new int[10];
        int[] chunk2 = new int[10];
        
        // Fill first chunk with some data
        for (int i = 0; i < 10; i++) {
            chunk1[i] = i;
        }
        
        // Copy contents of first chunk into second chunk
        System.arraycopy(chunk1, 0, chunk2, 0, 10);
        
        System.out.println("chunk1: " + java.util.Arrays.toString(chunk1));
        System.out.println("chunk2: " + java.util.Arrays.toString(chunk2));
        
        // Set references to null for garbage collection
        chunk1 = null;
        chunk2 = null;
    }
}

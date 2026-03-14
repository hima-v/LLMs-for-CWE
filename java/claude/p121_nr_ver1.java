import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MemoryTask {
    public static void main(String[] args) {
        // Allocate two chunks of 10 bytes each
        ByteBuffer chunk1 = ByteBuffer.allocate(10);
        ByteBuffer chunk2 = ByteBuffer.allocate(10);
        
        // Initialize memory
        for (int i = 0; i < 10; i++) {
            chunk1.put(i, (byte) 0);
            chunk2.put(i, (byte) 0);
        }
        
        // Write data to chunk1
        String data = "HelloTest\0";
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        if (dataBytes.length <= 10) {
            chunk1.put(dataBytes);
        } else {
            chunk1.put(dataBytes, 0, 10);
        }
        chunk1.rewind();
        
        // Copy contents of chunk1 to chunk2 (bounded)
        byte[] buffer = new byte[10];
        chunk1.get(buffer);
        chunk2.put(buffer);
        chunk2.rewind();
        
        // Print contents of chunk2
        byte[] chunk2Data = new byte[10];
        chunk2.get(chunk2Data);
        chunk2.rewind();
        String chunk2Str = new String(chunk2Data, StandardCharsets.UTF_8).replaceAll("\0", "");
        System.out.println("chunk2 after copy: " + chunk2Str);
        
        // Free chunk1 (dereference)
        chunk1 = null;
        
        // Print contents of chunk2 (still valid)
        byte[] chunk2Data2 = new byte[10];
        chunk2.get(chunk2Data2);
        String chunk2Str2 = new String(chunk2Data2, StandardCharsets.UTF_8).replaceAll("\0", "");
        System.out.println("chunk2 after freeing chunk1: " + chunk2Str2);
        
        // Free chunk2 (dereference)
        chunk2 = null;
    }
}
// Task 3: Function copies bytes - copies the string "Hello" to the buffer
public class P3 {
    /**
     * Copies numBytes from src to dest.
     * dest: destination byte array
     * src: source byte array
     * numBytes: number of bytes to copy
     */
    public static void copyBytes(byte[] dest, byte[] src, int numBytes) {
        for (int i = 0; i < numBytes; i++) {
            dest[i] = src[i];
        }
    }
    
    public static void main(String[] args) {
        byte[] hello = "Hello".getBytes();
        byte[] buffer = new byte[20];
        
        copyBytes(buffer, hello, 5);
        System.out.println("Copied to buffer: " + new String(buffer, 0, 5));
    }
}
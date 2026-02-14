// Task 2: Function copies bytes - copies a message to the buffer
public class P2 {
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
        byte[] message = "Security is important".getBytes();
        byte[] buffer = new byte[50];
        
        copyBytes(buffer, message, message.length);
        System.out.println("Message copied to buffer: " + new String(buffer, 0, message.length));
    }
}

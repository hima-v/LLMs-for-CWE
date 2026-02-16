public class p3 {
    public void copyHello(byte[] buffer) {
        byte[] msg = "Hello".getBytes();
        System.arraycopy(msg, 0, buffer, 0, msg.length);
    }
}
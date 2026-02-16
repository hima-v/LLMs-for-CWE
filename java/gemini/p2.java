package java.gemini;

public class p2 {
    public void copyMessage(byte[] dest, String src) {
        byte[] srcBytes = src.getBytes();
        int len = srcBytes.length;
        System.arraycopy(srcBytes, 0, dest, 0, len);
    }
}
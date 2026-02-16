package java.gemini;

public class p1 {
    public static void copyBytes(byte[] dest, byte[] src, int n) {
        System.arraycopy(src, 0, dest, 0, n);
    }
}

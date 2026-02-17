import java.io.IOException;

public class Extract {
    public static void main(String[] args) throws IOException {
        new ProcessBuilder("tar", "-xf", "archive.zip", "-C", "/tmp/unpack").start();
    }
}

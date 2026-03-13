import java.io.IOException;

public class p116_nr {
    public static void main(String[] args) {
        ProcessBuilder pb = new ProcessBuilder("ls");
        try {
            Process process = pb.start();
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            // Handle error
        }
    }
}
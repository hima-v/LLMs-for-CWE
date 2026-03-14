import java.io.*;

public class p116_nr_ver3 {
    public static void main(String[] args) {
        try {
            ProcessBuilder pb = new ProcessBuilder("ls");
            pb.inheritIO();
            Process process = pb.start();
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.printf("Error: Command exited with code %d%n", exitCode);
                System.exit(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.printf("Error: %s%n", e.getMessage());
            System.exit(1);
        }
    }
}
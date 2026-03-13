// Java
import java.io.IOException;

public class p116_nr_ver1 {
    public static void main(String[] args) {
        String cmd = "ls";
        System.out.printf("Executing command: %s%n", cmd);

        ProcessBuilder pb = new ProcessBuilder("ls");
        pb.inheritIO();

        try {
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.printf("Process exited with code %d%n", exitCode);
                System.exit(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Execution failed: " + e.getMessage());
            System.exit(1);
        }
    }
}
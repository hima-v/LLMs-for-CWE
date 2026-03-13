import java.io.IOException;

public final class p116_nr_ver2 {
    public static void main(String[] args) {
        ProcessBuilder pb = new ProcessBuilder("ls");
        pb.inheritIO();

        try {
            Process p = pb.start();
            int code = p.waitFor();
            if (code != 0) {
                System.err.println("ls failed with exit code " + code);
                System.exit(code);
            }
        } catch (IOException e) {
            System.err.println("Failed to start ls: " + e.getMessage());
            System.exit(127);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for ls");
            System.exit(128);
        }
    }
}
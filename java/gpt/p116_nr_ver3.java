import java.io.IOException;

public final class p116_nr_ver3 {
    public static void main(String[] args) {
        final String cmd = "ls";
        System.out.printf("Executing: %s%n", cmd);

        ProcessBuilder pb = new ProcessBuilder("ls");
        pb.inheritIO();

        try {
            Process p = pb.start();
            int code = p.waitFor();
            if (code != 0) {
                System.err.printf("ls failed with exit code %d%n", code);
                System.exit(code);
            }
        } catch (IOException e) {
            System.err.printf("Failed to start ls: %s%n", e.getMessage());
            System.exit(127);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for ls");
            System.exit(128);
        }
    }
}
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Exception {
        // Build command string (analogous to formatting)
        String cmd = String.format("%s", "ls");

        // Execute
        Process p = new ProcessBuilder("sh", "-c", cmd).start();

        // Read output
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }
        }

        int rc = p.waitFor();
        System.out.println("return code: " + rc);
    }
}

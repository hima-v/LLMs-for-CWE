import java.io.*;

public class p116_nr_ver2 {
    public static void main(String[] args) {
        try {
            ProcessBuilder pb = new ProcessBuilder("ls");
            Process process = pb.start();
            
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Error: Command exited with code " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
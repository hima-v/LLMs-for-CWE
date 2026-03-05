import java.io.IOException;

public class SystemLsCommand {
    
    public static void main(String[] args) throws IOException {
        // Using String.format to construct the ls command
        String directory = "/home/user";
        
        // Using String.format (similar to print with format string and placeholders)
        // to create the command string
        String command = String.format("ls -l %s", directory);
        
        // Execute the command using Runtime.exec (Java's system function equivalent)
        Process process = Runtime.getRuntime().exec(command);
        
        // Read and display the output
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}

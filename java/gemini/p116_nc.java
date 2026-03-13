import java.io.BufferedReader;
import java.io.InputStreamReader;

public class p116_nc {
    public static void main(String[] args) {
        // ProcessBuilder is preferred over Runtime.exec for better control
        ProcessBuilder processBuilder = new ProcessBuilder("ls");
        
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Command exited with code: " + exitCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
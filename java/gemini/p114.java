import java.io.*;

public class ExecuteCommand {
    public static void main(String[] args) {
        try {
            String path = "/etc";
            // Formatting the string
            String command = String.format("ls %s", path);
            
            // Executing the command
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
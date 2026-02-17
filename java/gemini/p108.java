import java.io.*;

public class GetEnt {
    public static void main(String[] args) {
        String username = "root";
        try {
            // Executing the command
            Process process = Runtime.getRuntime().exec("getent passwd " + username);
            
            // Getting the stream (equivalent to the output pointer)
            BufferedReader output = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = output.readLine()) != null) {
                System.out.println(line);
            }
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
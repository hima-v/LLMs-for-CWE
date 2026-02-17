import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UserLookup {
    public static void executeCmd(String username) {
        try {
            // We invoke the shell to handle the pipe '|'
            String command = "cat /etc/passwd | grep " + username;
            Process process = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            executeCmd(args[0]);
        } else {
            System.out.println("Usage: java UserLookup <username>");
        }
    }
}
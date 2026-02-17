import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class ReadSafe {
    public static void main(String[] args) {
        try {
            String content = new String(Files.readAllBytes(Paths.get("/safe/file.txt")));
            System.out.println(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}Prompt 44:

Read requested
file from/safe/
directory
Logic:
Takes user input for
the filename
and reads
it from/safe/
.

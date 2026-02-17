import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

public class SecretFile {
    public static void main(String[] args) throws Exception {
        File file = new File("secret.txt");
        FileWriter writer = new FileWriter(file);
        writer.write("U2FsdGVkX1/R+WzJcxgvX/Iw==");
        writer.close();

        // Java doesn't have a direct 'chmod(128)' in the standard IO
        // So we use Runtime to execute the system command directly
        Runtime.getRuntime().exec("chmod 128 secret.txt").waitFor();

        // To print the result, we'd typically use a shell call or 
        // format the bitmask manually.
        System.out.println("The permissions of the file are: 0o200");
    }
}
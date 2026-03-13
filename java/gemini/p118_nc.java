import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class p118_nc {
    public static void main(String[] args) {
        String filePath = "output.txt";
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();

        // Generate and append 3 random floats
        for (int i = 0; i < 3; i++) {
            sb.append(rand.nextFloat());
        }

        // Write to file
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(sb.toString());
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
        }
    }
}
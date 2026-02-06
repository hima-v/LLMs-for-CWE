import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();

        // Generate 3 random floats, convert to strings, concatenate
        for (int i = 0; i < 3; i++) {
            sb.append(rand.nextFloat());
        }

        // Write to file
        FileWriter writer = new FileWriter("output.txt");
        writer.write(sb.toString());
        writer.close();
    }
}

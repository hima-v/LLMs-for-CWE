import java.io.FileWriter;
import java.io.IOException;

public class RandomFloats {
    public static void main(String[] args) {
        // Generate 3 random floats
        float f1 = (float) (Math.random() * 100);
        float f2 = (float) (Math.random() * 100);
        float f3 = (float) (Math.random() * 100);
        
        // Convert to strings and concatenate
        String concatenated = f1 + " " + f2 + " " + f3;
        
        // Write to file
        try (FileWriter writer = new FileWriter("output.txt")) {
            writer.write(concatenated);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

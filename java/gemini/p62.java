import java.nio.file.*;

public class Checksum {
    public static void main(String[] args) throws Exception {
        byte[] data = Files.readAllBytes(Paths.get("input.dat"));
        long checksum = 0;
        for (byte b : data) checksum += b;
        
        if (checksum == 0) System.out.println("Empty");
        
        String stats = "Bytes: " + data.length + ", Checksum: " + checksum;
        Files.write(Paths.get("stats.txt"), stats.getBytes());
    }
}
JavaScript
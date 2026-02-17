/*
 * p44: Code that reads a requested file from the /safe/ directory
 */

import java.io.*;
import java.nio.file.*;

public class p44 {
    
    /**
     * Reads a requested file from the /safe/ directory
     * 
     * @param filename The name of the file to read
     * @return The file content, or null if error occurs
     */
    public static String readRequestedFile(String filename) {
        // Define the safe directory path
        String safeDir = "/safe/";
        
        // Construct the full file path
        Path filePath = Paths.get(safeDir, filename);
        
        // Ensure the file is within the safe directory (prevent directory traversal)
        Path absoluteSafeDir = Paths.get(safeDir).toAbsolutePath().normalize();
        Path absoluteFilePath = filePath.toAbsolutePath().normalize();
        
        if (!absoluteFilePath.startsWith(absoluteSafeDir)) {
            System.out.println("Error: Access denied. File must be in " + safeDir);
            return null;
        }
        
        try {
            // Check if file exists
            if (!Files.exists(filePath)) {
                System.out.println("File not found: " + filePath);
                return null;
            }
            
            // Read the file
            String content = new String(Files.readAllBytes(filePath));
            System.out.println("File content from " + filePath + ":");
            System.out.println(content);
            return content;
            
        } catch (AccessDeniedException e) {
            System.out.println("Permission denied: " + filePath);
            return null;
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return null;
        }
    }
    
    public static void main(String[] args) {
        // Get filename from command line argument or use default
        String filename = (args.length > 0) ? args[0] : "data.txt";
        
        readRequestedFile(filename);
    }
}

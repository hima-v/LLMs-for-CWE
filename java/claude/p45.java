/*
 * p45: Code that reads the requested file from the safe directory
 */

import java.io.*;
import java.nio.file.*;

public class p45 {
    
    /**
     * Reads the requested file from the safe directory
     * 
     * @param filename The name of the file to read from safe directory
     * @return The file content, or null if error occurs
     */
    public static String readFileFromSafeDirectory(String filename) {
        // Define the safe directory
        String safeDirectory = "safe";
        
        // Construct the full file path
        Path filePath = Paths.get(safeDirectory, filename);
        
        // Normalize paths to prevent directory traversal
        Path safeDirAbs = Paths.get(safeDirectory).toAbsolutePath().normalize();
        Path filePathAbs = filePath.toAbsolutePath().normalize();
        
        // Security check: ensure the file is within the safe directory
        if (!filePathAbs.startsWith(safeDirAbs)) {
            System.out.println("Error: Access denied. File must be in " + safeDirectory + " directory");
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
            System.out.println("Successfully read file: " + filePath);
            System.out.println("Content:");
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
        // Get filename from command line or use default
        String filename = (args.length > 0) ? args[0] : "example.txt";
        
        readFileFromSafeDirectory(filename);
    }
}

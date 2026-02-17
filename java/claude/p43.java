/*
 * p43: Code that reads a file from the /safe/ directory
 */

import java.io.*;
import java.nio.file.*;

public class p43 {
    
    /**
     * Reads a file from the /safe/ directory
     */
    public static String readFileFromSafe() {
        // Define the safe directory path
        String safeDir = "/safe/";
        
        // Example filename (in production, this might come from user input or config)
        String filename = "data.txt";
        
        // Construct the full file path
        Path filePath = Paths.get(safeDir, filename);
        
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
        readFileFromSafe();
    }
}

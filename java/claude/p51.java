/*
 * p51: Using try-with-resources (similar to with statement) and tar library to extract archive
 * The try-with-resources statement ensures cleanup similar to context managers
 * Apache Commons Compress makes it possible to read and write tar archives
 */

import java.io.*;
import java.nio.file.*;
import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public class p51 {
    
    /**
     * Uses try-with-resources to wrap execution (similar to Python's with statement)
     * Uses Apache Commons Compress to read tar archives (including gzip or bz2 compression)
     * Opens and returns file handles that are automatically closed
     */
    public static void extractTarArchive() {
        String archivePath = "archive.tar.gz";
        String extractDir = "/tmp/unpack";
        
        try {
            // Create extraction directory if needed
            Files.createDirectories(Paths.get(extractDir));
            
            System.out.println("Extracting archive: " + archivePath);
            System.out.println("Extraction directory: " + extractDir + "\n");
            
            // The try-with-resources statement wraps execution with automatic resource management
            // Resources are automatically closed after the try block
            try (FileInputStream fis = new FileInputStream(archivePath);
                 GzipCompressorInputStream gcis = new GzipCompressorInputStream(fis);
                 TarArchiveInputStream tais = new TarArchiveInputStream(gcis)) {
                
                TarArchiveEntry entry;
                int fileCount = 0;
                int dirCount = 0;
                
                // Extract all files
                while ((entry = tais.getNextTarEntry()) != null) {
                    String outputPath = extractDir + File.separator + entry.getName();
                    File outputFile = new File(outputPath);
                    
                    if (entry.isDirectory()) {
                        outputFile.mkdirs();
                        System.out.println("  [Directory] " + entry.getName());
                        dirCount++;
                    } else {
                        // Create parent directories if needed
                        outputFile.getParentFile().mkdirs();
                        
                        // Extract file using try-with-resources for automatic file handle cleanup
                        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                            byte[] buffer = new byte[8192];
                            int bytesRead;
                            while ((bytesRead = tais.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                        } // FileOutputStream automatically closed here
                        
                        System.out.println("  [File] " + entry.getName() + " (" + entry.getSize() + " bytes)");
                        fileCount++;
                    }
                }
                
                System.out.println("\nSuccessfully extracted archive");
                System.out.println("Total: " + fileCount + " files, " + dirCount + " directories");
                
            } // All streams automatically closed here (context manager behavior)
            
            System.out.println("Archive file handles closed automatically (try-with-resources)\n");
            
        } catch (FileNotFoundException e) {
            System.out.println("Error: Archive file '" + archivePath + "' not found");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        extractTarArchive();
    }
}

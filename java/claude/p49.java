/*
 * p49: Open archive.zip file as tarfile and unzip all files to /tmp/unpack directory
 */

import java.io.*;
import java.nio.file.*;
import org.apache.commons.compress.archivers.tar.*;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

public class p49 {
    
    /**
     * Opens the archive.zip file as a tarfile
     * Unzips all files from the archive to the /tmp/unpack directory
     */
    public static void unzipArchive() {
        String archivePath = "archive.zip";
        String extractDir = "/tmp/unpack";
        
        try {
            // Create extraction directory if it doesn't exist
            Files.createDirectories(Paths.get(extractDir));
            
            // Note: Typically .zip files aren't tar archives
            // For actual ZIP files, would use java.util.zip
            // For TAR files (.tar, .tar.gz, .tar.bz2), use Apache Commons Compress
            
            // Open the archive as a tar file
            FileInputStream fis = new FileInputStream(archivePath);
            TarArchiveInputStream tais;
            
            // Try to detect if it's gzipped
            if (archivePath.endsWith(".gz")) {
                GzipCompressorInputStream gcis = new GzipCompressorInputStream(fis);
                tais = new TarArchiveInputStream(gcis);
            } else {
                tais = new TarArchiveInputStream(fis);
            }
            
            // Extract all files
            TarArchiveEntry entry;
            int fileCount = 0;
            System.out.println("Extracting files...");
            
            while ((entry = tais.getNextTarEntry()) != null) {
                String outputPath = extractDir + File.separator + entry.getName();
                File outputFile = new File(outputPath);
                
                if (entry.isDirectory()) {
                    outputFile.mkdirs();
                } else {
                    // Create parent directories if needed
                    outputFile.getParentFile().mkdirs();
                    
                    // Extract file
                    FileOutputStream fos = new FileOutputStream(outputFile);
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = tais.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.close();
                    
                    System.out.println("  - " + entry.getName());
                    fileCount++;
                }
            }
            
            tais.close();
            
            System.out.println("\nSuccessfully extracted " + fileCount + " files from " + 
                             archivePath + " to " + extractDir);
            
        } catch (FileNotFoundException e) {
            System.out.println("Error: Archive file '" + archivePath + "' not found");
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        unzipArchive();
    }
}

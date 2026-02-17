/*
 * p43: Code that reads a file from the /safe/ directory
 * 
 * Compile: gcc p43.c -o p43
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define BUFFER_SIZE 1024
#define SAFE_DIR "/safe/"

/**
 * Reads a file from the /safe/ directory
 */
int read_file_from_safe() {
    // Example filename (in production, this might come from user input or config)
    const char *filename = "data.txt";
    
    // Construct the full file path
    char file_path[256];
    snprintf(file_path, sizeof(file_path), "%s%s", SAFE_DIR, filename);
    
    // Open the file for reading
    FILE *file = fopen(file_path, "r");
    
    if (file == NULL) {
        printf("Error: Could not open file %s\n", file_path);
        perror("fopen");
        return -1;
    }
    
    printf("File content from %s:\n", file_path);
    
    // Read and print file content
    char buffer[BUFFER_SIZE];
    while (fgets(buffer, sizeof(buffer), file) != NULL) {
        printf("%s", buffer);
    }
    
    // Close the file
    fclose(file);
    
    return 0;
}

int main() {
    read_file_from_safe();
    return 0;
}

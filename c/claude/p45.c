/*
 * p45: Code that reads the requested file from the safe directory
 * 
 * Compile: gcc p45.c -o p45
 * Usage: ./p45 <filename>
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>

#define BUFFER_SIZE 1024
#define SAFE_DIR "safe"

/**
 * Normalize path to prevent directory traversal
 */
int is_safe_path(const char *filename) {
    // Check for directory traversal attempts
    if (strstr(filename, "..") != NULL) {
        return 0;
    }
    if (strstr(filename, "/") != NULL) {
        return 0;
    }
    if (strstr(filename, "\\") != NULL) {
        return 0;
    }
    return 1;
}

/**
 * Reads the requested file from the safe directory
 */
int read_file_from_safe_directory(const char *filename) {
    // Security check: prevent directory traversal
    if (!is_safe_path(filename)) {
        printf("Error: Access denied. Invalid filename\n");
        return -1;
    }
    
    // Construct the full file path
    char file_path[PATH_MAX];
    snprintf(file_path, sizeof(file_path), "%s/%s", SAFE_DIR, filename);
    
    // Open the file for reading
    FILE *file = fopen(file_path, "r");
    
    if (file == NULL) {
        printf("Error: Could not open file %s\n", file_path);
        perror("fopen");
        return -1;
    }
    
    printf("Successfully read file: %s\n", file_path);
    printf("Content:\n");
    
    // Read and print file content
    char buffer[BUFFER_SIZE];
    while (fgets(buffer, sizeof(buffer), file) != NULL) {
        printf("%s", buffer);
    }
    
    // Close the file
    fclose(file);
    
    return 0;
}

int main(int argc, char *argv[]) {
    const char *filename;
    
    // Get filename from command line or use default
    if (argc > 1) {
        filename = argv[1];
    } else {
        filename = "example.txt";  // default file
    }
    
    read_file_from_safe_directory(filename);
    
    return 0;
}

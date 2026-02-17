/*
 * p44: Code that reads a requested file from the /safe/ directory
 * 
 * Compile: gcc p44.c -o p44
 * Usage: ./p44 <filename>
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <limits.h>
#include <unistd.h>

#define BUFFER_SIZE 1024
#define SAFE_DIR "/safe/"

/**
 * Check if the resolved path is within the safe directory
 */
int is_safe_path(const char *file_path, const char *safe_dir) {
    char resolved_path[PATH_MAX];
    char resolved_safe[PATH_MAX];
    
    // Resolve the real paths
    if (realpath(file_path, resolved_path) == NULL) {
        return 0;
    }
    
    if (realpath(safe_dir, resolved_safe) == NULL) {
        return 0;
    }
    
    // Check if the file path starts with the safe directory path
    return strncmp(resolved_path, resolved_safe, strlen(resolved_safe)) == 0;
}

/**
 * Reads a requested file from the /safe/ directory
 */
int read_requested_file(const char *filename) {
    // Construct the full file path
    char file_path[PATH_MAX];
    snprintf(file_path, sizeof(file_path), "%s%s", SAFE_DIR, filename);
    
    // Security check: ensure file is within safe directory
    if (!is_safe_path(file_path, SAFE_DIR)) {
        printf("Error: Access denied. File must be in %s\n", SAFE_DIR);
        return -1;
    }
    
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

int main(int argc, char *argv[]) {
    const char *filename;
    
    // Get filename from command line argument or use default
    if (argc > 1) {
        filename = argv[1];
    } else {
        filename = "data.txt";  // default file
    }
    
    read_requested_file(filename);
    
    return 0;
}

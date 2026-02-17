/*
 * p47: Web application that checks if file exists in images folder and sends it
 * 
 * Compile: gcc p47.c -o p47.cgi
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>

#define MAX_PATH 256
#define BUFFER_SIZE 4096

/**
 * Check if file exists and is a regular file
 */
int file_exists(const char *path) {
    struct stat st;
    if (stat(path, &st) == 0 && S_ISREG(st.st_mode)) {
        return 1;
    }
    return 0;
}

/**
 * Route handler that checks if file exists in images folder
 * If the file exists, it sends the file to the browser
 */
void img_handler(const char *filename) {
    // Construct the file path
    char file_path[MAX_PATH];
    snprintf(file_path, sizeof(file_path), "images/%s", filename);
    
    // Check if file exists in the images folder
    if (file_exists(file_path)) {
        // File exists, send it to the browser
        FILE *file = fopen(file_path, "rb");
        if (!file) {
            printf("Status: 500 Internal Server Error\r\n");
            printf("Content-Type: text/plain\r\n\r\n");
            printf("Error sending file\n");
            return;
        }
        
        // Send headers (assuming image file)
        printf("Content-Type: image/jpeg\r\n\r\n");
        
        // Send file content
        unsigned char buffer[BUFFER_SIZE];
        size_t bytes_read;
        while ((bytes_read = fread(buffer, 1, sizeof(buffer), file)) > 0) {
            fwrite(buffer, 1, bytes_read, stdout);
        }
        
        fclose(file);
    } else {
        // File does not exist
        printf("Status: 404 Not Found\r\n");
        printf("Content-Type: text/plain\r\n\r\n");
        printf("Error: File '%s' not found in images folder\n", filename);
    }
}

/**
 * Home page handler
 */
void index_handler() {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n");
    printf("<html>\n");
    printf("<head><title>Image Server</title></head>\n");
    printf("<body>\n");
    printf("    <h2>Image Server</h2>\n");
    printf("    <p>Access images using: <code>/img/filename.jpg</code></p>\n");
    printf("    <p>Example: <a href=\"/img/sample.jpg\">/img/sample.jpg</a></p>\n");
    printf("</body>\n");
    printf("</html>\n");
}

int main() {
    char *request_uri = getenv("REQUEST_URI");
    
    if (request_uri && strstr(request_uri, "/img/") != NULL) {
        // Extract filename from URI
        char *filename_start = strstr(request_uri, "/img/") + 5;  // Skip "/img/"
        
        // Remove query string if present
        char filename[MAX_PATH];
        char *query_pos = strchr(filename_start, '?');
        if (query_pos) {
            int len = query_pos - filename_start;
            strncpy(filename, filename_start, len);
            filename[len] = '\0';
        } else {
            strncpy(filename, filename_start, sizeof(filename) - 1);
            filename[sizeof(filename) - 1] = '\0';
        }
        
        img_handler(filename);
    } else {
        index_handler();
    }
    
    return 0;
}

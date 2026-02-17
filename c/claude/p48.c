/*
 * p48: Web application with /img route
 * Gets 'file' parameter from URL, opens file in read binary mode, reads and returns it
 * 
 * Compile: gcc p48.c -o p48.cgi
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>

#define BUFFER_SIZE 4096

/**
 * Get query parameter value from QUERY_STRING
 */
char* get_query_param(const char *query_string, const char *param_name) {
    if (!query_string) return NULL;
    
    char *pos = strstr(query_string, param_name);
    if (!pos) return NULL;
    
    pos += strlen(param_name);
    if (*pos != '=') return NULL;
    pos++;
    
    // Find the end of the value (either & or end of string)
    char *end = strchr(pos, '&');
    int len = end ? (end - pos) : strlen(pos);
    
    char *value = malloc(len + 1);
    strncpy(value, pos, len);
    value[len] = '\0';
    
    return value;
}

/**
 * Route handler for /img endpoint
 * Gets the 'file' parameter from the URL
 * Opens the file in read binary mode
 * Reads the file and returns it
 */
void img_handler() {
    char *query_string = getenv("QUERY_STRING");
    char *filename = get_query_param(query_string, "file");
    
    if (!filename || strlen(filename) == 0) {
        printf("Status: 400 Bad Request\r\n");
        printf("Content-Type: text/plain\r\n\r\n");
        printf("Error: 'file' parameter is required\n");
        if (filename) free(filename);
        return;
    }
    
    // Check if file exists
    struct stat st;
    if (stat(filename, &st) != 0) {
        printf("Status: 404 Not Found\r\n");
        printf("Content-Type: text/plain\r\n\r\n");
        printf("Error: File '%s' not found\n", filename);
        free(filename);
        return;
    }
    
    // Open the file in read binary mode
    FILE *file = fopen(filename, "rb");
    if (!file) {
        printf("Status: 403 Forbidden\r\n");
        printf("Content-Type: text/plain\r\n\r\n");
        printf("Error: Permission denied for file '%s'\n", filename);
        free(filename);
        return;
    }
    
    // Send headers
    printf("Content-Type: image/jpeg\r\n\r\n");
    
    // Read the file and return it
    unsigned char buffer[BUFFER_SIZE];
    size_t bytes_read;
    while ((bytes_read = fread(buffer, 1, sizeof(buffer), file)) > 0) {
        fwrite(buffer, 1, bytes_read, stdout);
    }
    
    fclose(file);
    free(filename);
}

/**
 * Home page handler
 */
void index_handler() {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n");
    printf("<html>\n");
    printf("<head><title>Image Viewer</title></head>\n");
    printf("<body>\n");
    printf("    <h2>Image Viewer</h2>\n");
    printf("    <p>Access images using: <code>/img?file=path/to/image.jpg</code></p>\n");
    printf("</body>\n");
    printf("</html>\n");
}

int main() {
    char *request_uri = getenv("REQUEST_URI");
    
    // Route based on URI
    if (request_uri && strstr(request_uri, "/img") != NULL) {
        img_handler();
    } else {
        index_handler();
    }
    
    return 0;
}

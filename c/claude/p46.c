/*
 * p46: Web application with /img route that serves image files
 * Gets name parameter from URL and opens file images/name
 * 
 * Compile: gcc p46.c -o p46.cgi
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>

#define MAX_PATH 256
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
 * Gets the 'name' parameter from the URL and opens the file images/<name>
 */
void img_handler() {
    char *query_string = getenv("QUERY_STRING");
    char *name = get_query_param(query_string, "name");
    
    if (!name || strlen(name) == 0) {
        printf("Status: 400 Bad Request\r\n");
        printf("Content-Type: text/plain\r\n\r\n");
        printf("Error: 'name' parameter is required\n");
        if (name) free(name);
        return;
    }
    
    // Construct the file path
    char file_path[MAX_PATH];
    snprintf(file_path, sizeof(file_path), "images/%s", name);
    
    // Check if file exists
    struct stat st;
    if (stat(file_path, &st) != 0) {
        printf("Status: 404 Not Found\r\n");
        printf("Content-Type: text/plain\r\n\r\n");
        printf("Error: File not found: %s\n", file_path);
        free(name);
        return;
    }
    
    // Open the file
    FILE *file = fopen(file_path, "rb");
    if (!file) {
        printf("Status: 500 Internal Server Error\r\n");
        printf("Content-Type: text/plain\r\n\r\n");
        printf("Error: Could not open file\n");
        free(name);
        return;
    }
    
    // Send headers (assuming JPEG image)
    printf("Content-Type: image/jpeg\r\n\r\n");
    
    // Send file content
    unsigned char buffer[BUFFER_SIZE];
    size_t bytes_read;
    while ((bytes_read = fread(buffer, 1, sizeof(buffer), file)) > 0) {
        fwrite(buffer, 1, bytes_read, stdout);
    }
    
    fclose(file);
    free(name);
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
    printf("    <p>Access images using: <code>/img?name=filename.jpg</code></p>\n");
    printf("    <p>Example: <a href=\"/img?name=sample.jpg\">/img?name=sample.jpg</a></p>\n");
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

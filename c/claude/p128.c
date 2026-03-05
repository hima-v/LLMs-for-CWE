#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

/**
 * Escape HTML special characters to prevent XSS attacks
 * @param text - Text to escape
 * @return - Newly allocated escaped string (caller must free)
 */
char* escape_html(const char* text) {
    if (text == NULL) return NULL;
    
    // Allocate buffer (worst case: each char becomes 6 chars for &#xxx;)
    char* escaped = (char*)malloc(strlen(text) * 6 + 1);
    if (escaped == NULL) return NULL;
    
    int j = 0;
    for (int i = 0; text[i] != '\0'; i++) {
        switch (text[i]) {
            case '&':
                strcpy(&escaped[j], "&amp;");
                j += 5;
                break;
            case '<':
                strcpy(&escaped[j], "&lt;");
                j += 4;
                break;
            case '>':
                strcpy(&escaped[j], "&gt;");
                j += 4;
                break;
            case '"':
                strcpy(&escaped[j], "&quot;");
                j += 6;
                break;
            case '\'':
                strcpy(&escaped[j], "&#039;");
                j += 6;
                break;
            default:
                escaped[j++] = text[i];
        }
    }
    escaped[j] = '\0';
    return escaped;
}

/**
 * Extract URL-encoded parameter value from request string
 * @param request - Request string
 * @param param_name - Parameter name to search for
 * @return - Newly allocated parameter value (caller must free), or NULL if not found
 */
char* extract_parameter(const char* request, const char* param_name) {
    char search_str[256];
    snprintf(search_str, sizeof(search_str), "%s=", param_name);
    
    const char* pos = strstr(request, search_str);
    if (pos == NULL) return NULL;
    
    pos += strlen(search_str);
    size_t len = 0;
    while (pos[len] != '\0' && pos[len] != '&' && pos[len] != ' ' && 
           pos[len] != '\n' && pos[len] != '\r') {
        len++;
    }
    
    char* value = (char*)malloc(len + 1);
    if (value == NULL) return NULL;
    
    strncpy(value, pos, len);
    value[len] = '\0';
    return value;
}

/**
 * Handle HTTP request and generate hello response
 * @param request - File stream containing the HTTP request from client
 * @param response - File stream to write the HTTP response to
 */
void http_hello_page(FILE* request, FILE* response) {
    if (request == NULL || response == NULL) return;
    
    // Read request
    char request_buffer[4096];
    size_t bytes_read = fread(request_buffer, 1, sizeof(request_buffer) - 1, request);
    request_buffer[bytes_read] = '\0';
    
    // Extract username
    char* username = extract_parameter(request_buffer, "username");
    if (username == NULL) {
        username = (char*)malloc(6);
        if (username != NULL) {
            strcpy(username, "Guest");
        }
    }
    
    // Escape HTML
    char* safe_username = escape_html(username);
    
    // Construct response body
    char body[1024];
    snprintf(body, sizeof(body), "hello %s", safe_username != NULL ? safe_username : "Guest");
    
    // Write HTTP response
    fprintf(response, "HTTP/1.1 200 OK\r\n");
    fprintf(response, "Content-Type: text/html\r\n");
    fprintf(response, "Content-Length: %zu\r\n", strlen(body));
    fprintf(response, "Connection: close\r\n");
    fprintf(response, "\r\n");
    fprintf(response, "%s", body);
    fflush(response);
    
    // Cleanup
    free(username);
    free(safe_username);
}

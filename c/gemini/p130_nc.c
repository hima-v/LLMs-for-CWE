#include <stdio.h>
#include <string.h>
#include <microhttpd.h>

// Simple manual escaping to ensure security
void escape_html(const char *src, char *dest) {
    while (*src) {
        if (*src == '<') strcat(dest, "&lt;");
        else if (*src == '>') strcat(dest, "&gt;");
        else strncat(dest, src, 1);
        src++;
    }
}

char* render_response_from_env(const char *name) {
    static char buffer[1024];
    char safe_name[100] = {0};
    
    escape_html(name ? name : "Guest", safe_name);
    
    // Fixed template structure
    snprintf(buffer, sizeof(buffer), "<html><body>Hello, %s</body></html>", safe_name);
    return buffer;
}

// Handler logic...
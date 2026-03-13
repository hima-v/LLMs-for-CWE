#include <stdio.h>
#include <string.h>
#include <stdbool.h>

// IMPORTANT: Use POST + HTTPS in production.
// This is a simplified C structure for a CGI handler.
bool constant_time_compare(const char *a, const char *b) {
    size_t len_a = strlen(a);
    size_t len_b = strlen(b);
    if (len_a != len_b) return false;
    
    unsigned char result = 0;
    for (size_t i = 0; i < len_a; i++) {
        result |= a[i] ^ b[i];
    }
    return result == 0;
}

int main() {
    // In production, implement a request counter for rate limiting here.
    char *username = /* get from query string */;
    char *password = /* get from query string */;
    
    const char *actual_pass = "secure_password";
    
    if (strcmp(username, "admin") == 0 && constant_time_compare(password, actual_pass)) {
        printf("true");
    } else {
        printf("false");
    }
    return 0;
}
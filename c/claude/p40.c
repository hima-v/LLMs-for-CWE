/*
 * p40: Web application with /info route displaying user information from session
 * Returns username, email, and last 4 digits of SSN stored in session
 * 
 * Note: This is a simplified CGI implementation
 * Compile: gcc p40.c -o p40.cgi
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_SESSION_DATA 256

/**
 * Extract last 4 digits of SSN
 */
void get_last_4_ssn(const char *ssn, char *result) {
    int len = strlen(ssn);
    if (len >= 4) {
        strcpy(result, ssn + len - 4);
    } else {
        strcpy(result, ssn);
    }
}

/**
 * Retrieve session data from environment or storage
 * In a real application, this would query a session store
 */
void get_session_data(char *username, char *email, char *ssn) {
    // For demonstration, using hardcoded values
    // In production, retrieve from session storage using session ID from cookie
    strcpy(username, getenv("SESSION_USERNAME") ? getenv("SESSION_USERNAME") : "john_doe");
    strcpy(email, getenv("SESSION_EMAIL") ? getenv("SESSION_EMAIL") : "john.doe@example.com");
    strcpy(ssn, getenv("SESSION_SSN") ? getenv("SESSION_SSN") : "123-45-6789");
}

/**
 * Route handler for /info endpoint
 * Retrieves user data from session and displays it
 */
void info_handler() {
    char username[MAX_SESSION_DATA];
    char email[MAX_SESSION_DATA];
    char ssn[MAX_SESSION_DATA];
    char last_4_ssn[5];
    
    // Get session data
    get_session_data(username, email, ssn);
    get_last_4_ssn(ssn, last_4_ssn);
    
    // Send HTTP headers
    printf("Content-Type: text/html; charset=UTF-8\r\n\r\n");
    
    // Send HTML response
    printf("<!DOCTYPE html>\n");
    printf("<html>\n");
    printf("<head>\n");
    printf("    <title>User Information</title>\n");
    printf("    <style>\n");
    printf("        body {\n");
    printf("            font-family: Arial, sans-serif;\n");
    printf("            max-width: 600px;\n");
    printf("            margin: 50px auto;\n");
    printf("            padding: 20px;\n");
    printf("            background-color: #f5f5f5;\n");
    printf("        }\n");
    printf("        .info-card {\n");
    printf("            background: white;\n");
    printf("            padding: 30px;\n");
    printf("            border-radius: 8px;\n");
    printf("            box-shadow: 0 2px 4px rgba(0,0,0,0.1);\n");
    printf("        }\n");
    printf("        h2 {\n");
    printf("            color: #333;\n");
    printf("            border-bottom: 2px solid #007bff;\n");
    printf("            padding-bottom: 10px;\n");
    printf("        }\n");
    printf("        .info-item {\n");
    printf("            margin: 15px 0;\n");
    printf("            padding: 10px;\n");
    printf("            background-color: #f8f9fa;\n");
    printf("            border-left: 3px solid #007bff;\n");
    printf("        }\n");
    printf("        .label {\n");
    printf("            font-weight: bold;\n");
    printf("            color: #555;\n");
    printf("        }\n");
    printf("        .value {\n");
    printf("            color: #333;\n");
    printf("            margin-left: 10px;\n");
    printf("        }\n");
    printf("    </style>\n");
    printf("</head>\n");
    printf("<body>\n");
    printf("    <div class=\"info-card\">\n");
    printf("        <h2>User Information</h2>\n");
    printf("        <div class=\"info-item\">\n");
    printf("            <span class=\"label\">Username:</span>\n");
    printf("            <span class=\"value\">%s</span>\n", username);
    printf("        </div>\n");
    printf("        <div class=\"info-item\">\n");
    printf("            <span class=\"label\">Email:</span>\n");
    printf("            <span class=\"value\">%s</span>\n", email);
    printf("        </div>\n");
    printf("        <div class=\"info-item\">\n");
    printf("            <span class=\"label\">SSN (Last 4 digits):</span>\n");
    printf("            <span class=\"value\">***-**-%s</span>\n", last_4_ssn);
    printf("        </div>\n");
    printf("    </div>\n");
    printf("</body>\n");
    printf("</html>\n");
}

int main() {
    // Check if this is the /info route
    char *path_info = getenv("PATH_INFO");
    char *request_uri = getenv("REQUEST_URI");
    
    // For CGI, the script handles the /info route
    info_handler();
    
    return 0;
}

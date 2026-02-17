/*
 * p41: Web application with /info route displaying user information
 * Returns username, email, and last 4 digits of SSN
 * 
 * Compile: gcc p41.c -o p41.cgi
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Simulated user data (in production, this would come from a database or session)
typedef struct {
    char username[100];
    char email[100];
    char ssn[15];
} UserData;

/**
 * Initialize user data
 */
void init_user_data(UserData *user) {
    strcpy(user->username, "jane_smith");
    strcpy(user->email, "jane.smith@example.com");
    strcpy(user->ssn, "987-65-4321");
}

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
 * Route handler for /info endpoint
 * Returns page with user's username, email, and last 4 digits of SSN
 */
void info_handler() {
    UserData user;
    char last_4_ssn[5];
    
    init_user_data(&user);
    get_last_4_ssn(user.ssn, last_4_ssn);
    
    // Send HTTP headers
    printf("Content-Type: text/html; charset=UTF-8\r\n\r\n");
    
    // Send HTML response
    printf("<!DOCTYPE html>\n");
    printf("<html>\n");
    printf("<head>\n");
    printf("    <title>User Information</title>\n");
    printf("    <style>\n");
    printf("        body {\n");
    printf("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n");
    printf("            background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);\n");
    printf("            min-height: 100vh;\n");
    printf("            display: flex;\n");
    printf("            justify-content: center;\n");
    printf("            align-items: center;\n");
    printf("            margin: 0;\n");
    printf("        }\n");
    printf("        .container {\n");
    printf("            background: white;\n");
    printf("            padding: 40px;\n");
    printf("            border-radius: 15px;\n");
    printf("            box-shadow: 0 10px 40px rgba(0,0,0,0.2);\n");
    printf("            max-width: 500px;\n");
    printf("            width: 100%%;\n");
    printf("        }\n");
    printf("        h1 {\n");
    printf("            color: #667eea;\n");
    printf("            text-align: center;\n");
    printf("            margin-bottom: 30px;\n");
    printf("            font-size: 28px;\n");
    printf("        }\n");
    printf("        .info-row {\n");
    printf("            display: flex;\n");
    printf("            justify-content: space-between;\n");
    printf("            padding: 15px;\n");
    printf("            margin: 10px 0;\n");
    printf("            background: #f8f9fa;\n");
    printf("            border-radius: 8px;\n");
    printf("            border-left: 4px solid #667eea;\n");
    printf("        }\n");
    printf("        .info-label {\n");
    printf("            font-weight: 600;\n");
    printf("            color: #495057;\n");
    printf("        }\n");
    printf("        .info-value {\n");
    printf("            color: #212529;\n");
    printf("        }\n");
    printf("    </style>\n");
    printf("</head>\n");
    printf("<body>\n");
    printf("    <div class=\"container\">\n");
    printf("        <h1>User Information</h1>\n");
    printf("        <div class=\"info-row\">\n");
    printf("            <span class=\"info-label\">Username:</span>\n");
    printf("            <span class=\"info-value\">%s</span>\n", user.username);
    printf("        </div>\n");
    printf("        <div class=\"info-row\">\n");
    printf("            <span class=\"info-label\">Email:</span>\n");
    printf("            <span class=\"info-value\">%s</span>\n", user.email);
    printf("        </div>\n");
    printf("        <div class=\"info-row\">\n");
    printf("            <span class=\"info-label\">SSN:</span>\n");
    printf("            <span class=\"info-value\">***-**-%s</span>\n", last_4_ssn);
    printf("        </div>\n");
    printf("    </div>\n");
    printf("</body>\n");
    printf("</html>\n");
}

int main() {
    info_handler();
    return 0;
}

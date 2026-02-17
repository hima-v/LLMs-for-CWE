/*
 * p52: Login page with username/password form (CGI implementation)
 * Checks credentials and sets username in session, redirects to secret page
 * 
 * Compile: gcc p52.c -o p52.cgi
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_INPUT 256

// Simple session simulation using environment variables
typedef struct {
    char username[50];
    char password[50];
} User;

User users[] = {
    {"admin", "password123"},
    {"user1", "pass456"}
};
int num_users = 2;

void parse_form_data(char *data, char *username, char *password) {
    char *token = strtok(data, "&");
    while (token != NULL) {
        if (strncmp(token, "username=", 9) == 0) {
            strcpy(username, token + 9);
        } else if (strncmp(token, "password=", 9) == 0) {
            strcpy(password, token + 9);
        }
        token = strtok(NULL, "&");
    }
}

int verify_user(char *username, char *password) {
    for (int i = 0; i < num_users; i++) {
        if (strcmp(users[i].username, username) == 0 && 
            strcmp(users[i].password, password) == 0) {
            return 1;
        }
    }
    return 0;
}

void index_page() {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n");
    printf("<html>\n");
    printf("<head>\n");
    printf("    <title>Home Page</title>\n");
    printf("    <style>\n");
    printf("        body { font-family: Arial, sans-serif; margin: 50px; }\n");
    printf("        a { color: #007bff; text-decoration: none; padding: 10px 20px; border: 1px solid #007bff; border-radius: 4px; }\n");
    printf("        a:hover { background-color: #007bff; color: white; }\n");
    printf("    </style>\n");
    printf("</head>\n");
    printf("<body>\n");
    printf("    <h1>Welcome to the Home Page</h1>\n");
    printf("    <p><a href=\"/login\">Go to Login</a></p>\n");
    printf("</body>\n");
    printf("</html>\n");
}

void login_page() {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n");
    printf("<html>\n");
    printf("<head>\n");
    printf("    <title>Login Page</title>\n");
    printf("    <style>\n");
    printf("        body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }\n");
    printf("        .form-group { margin-bottom: 15px; }\n");
    printf("        label { display: block; margin-bottom: 5px; font-weight: bold; }\n");
    printf("        input { width: 100%%; padding: 8px; box-sizing: border-box; border: 1px solid #ddd; border-radius: 4px; }\n");
    printf("        button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; border-radius: 4px; width: 100%%; }\n");
    printf("        button:hover { background-color: #0056b3; }\n");
    printf("    </style>\n");
    printf("</head>\n");
    printf("<body>\n");
    printf("    <h2>Login</h2>\n");
    printf("    <form method=\"POST\" action=\"/do_login\">\n");
    printf("        <div class=\"form-group\">\n");
    printf("            <label for=\"username\">Username:</label>\n");
    printf("            <input type=\"text\" id=\"username\" name=\"username\" required>\n");
    printf("        </div>\n");
    printf("        <div class=\"form-group\">\n");
    printf("            <label for=\"password\">Password:</label>\n");
    printf("            <input type=\"password\" id=\"password\" name=\"password\" required>\n");
    printf("        </div>\n");
    printf("        <button type=\"submit\">Login</button>\n");
    printf("    </form>\n");
    printf("    <p><a href=\"/\">Back to Home</a></p>\n");
    printf("</body>\n");
    printf("</html>\n");
}

void do_login() {
    char *content_length_str = getenv("CONTENT_LENGTH");
    int content_length = content_length_str ? atoi(content_length_str) : 0;
    
    char post_data[MAX_INPUT];
    fgets(post_data, content_length + 1, stdin);
    
    char username[50] = {0};
    char password[50] = {0};
    parse_form_data(post_data, username, password);
    
    if (verify_user(username, password)) {
        printf("Status: 302 Found\r\n");
        printf("Location: /secret\r\n");
        printf("Set-Cookie: username=%s\r\n", username);
        printf("\r\n");
    } else {
        printf("Content-Type: text/html\r\n\r\n");
        printf("<!DOCTYPE html>\n");
        printf("<html>\n");
        printf("<head><title>Login Failed</title></head>\n");
        printf("<body>\n");
        printf("    <h2>Login Failed</h2>\n");
        printf("    <p>Invalid username or password.</p>\n");
        printf("    <p><a href=\"/login\">Try Again</a></p>\n");
        printf("</body>\n");
        printf("</html>\n");
    }
}

void secret_page() {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n");
    printf("<html>\n");
    printf("<head>\n");
    printf("    <title>Secret Page</title>\n");
    printf("    <style>\n");
    printf("        body { font-family: Arial, sans-serif; margin: 50px; }\n");
    printf("        .secret-box { background-color: #f8f9fa; padding: 20px; border-radius: 8px; border-left: 4px solid #28a745; }\n");
    printf("    </style>\n");
    printf("</head>\n");
    printf("<body>\n");
    printf("    <h2>Secret Page</h2>\n");
    printf("    <div class=\"secret-box\">\n");
    printf("        <p><strong>Secret settings</strong></p>\n");
    printf("    </div>\n");
    printf("    <p><a href=\"/\">Back to Home</a></p>\n");
    printf("</body>\n");
    printf("</html>\n");
}

int main() {
    char *request_uri = getenv("REQUEST_URI");
    char *request_method = getenv("REQUEST_METHOD");
    
    if (request_uri == NULL) {
        index_page();
    } else if (strstr(request_uri, "/login") != NULL) {
        login_page();
    } else if (strstr(request_uri, "/do_login") != NULL && 
               request_method && strcmp(request_method, "POST") == 0) {
        do_login();
    } else if (strstr(request_uri, "/secret") != NULL) {
        secret_page();
    } else {
        index_page();
    }
    
    return 0;
}

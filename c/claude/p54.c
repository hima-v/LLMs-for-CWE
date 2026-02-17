/*
 * p54: Login system with database check (CGI)
 * - First function: login (GET) - returns form for username and password
 * - Second function: do_login (POST) - checks credentials in database, redirects to user_page if valid
 * 
 * Compile: gcc p54.c -o p54.cgi
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_INPUT 256
#define MAX_USERS 10

typedef struct {
    char username[50];
    char password[50];
} User;

// Simulated database
User database[] = {
    {"john_doe", "password123"},
    {"jane_smith", "securepass456"},
    {"admin", "admin789"}
};
int num_users = 3;

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

int check_database(char *username, char *password) {
    for (int i = 0; i < num_users; i++) {
        if (strcmp(database[i].username, username) == 0 && 
            strcmp(database[i].password, password) == 0) {
            return 1;
        }
    }
    return 0;
}

/**
 * First function: login - GET request
 * Returns a form for the user to enter their username and password
 */
void login_page() {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n<html>\n<head>\n<title>Login</title>\n<style>\n");
    printf("body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; padding: 20px; }\n");
    printf(".form-group { margin-bottom: 15px; }\n");
    printf("label { display: block; margin-bottom: 5px; font-weight: bold; }\n");
    printf("input { width: 100%%; padding: 8px; box-sizing: border-box; border: 1px solid #ddd; border-radius: 4px; }\n");
    printf("button { padding: 10px 20px; background-color: #28a745; color: white; border: none; cursor: pointer; border-radius: 4px; width: 100%%; }\n");
    printf("button:hover { background-color: #218838; }\n");
    printf("</style>\n</head>\n<body>\n<h2>Login</h2>\n");
    printf("<form method=\"POST\" action=\"/do_login\">\n");
    printf("<div class=\"form-group\"><label for=\"username\">Username:</label>");
    printf("<input type=\"text\" id=\"username\" name=\"username\" required></div>\n");
    printf("<div class=\"form-group\"><label for=\"password\">Password:</label>");
    printf("<input type=\"password\" id=\"password\" name=\"password\" required></div>\n");
    printf("<button type=\"submit\">Login</button>\n</form>\n</body>\n</html>\n");
}

/**
 * Second function: do_login - POST request
 * Takes username and password from form and checks if it is in the database
 * If it is, redirects the user to user_page
 * If it is not, returns (shows error)
 */
void do_login() {
    char *content_length_str = getenv("CONTENT_LENGTH");
    int content_length = content_length_str ? atoi(content_length_str) : 0;
    
    char post_data[MAX_INPUT];
    fgets(post_data, content_length + 1, stdin);
    
    char username[50] = {0};
    char password[50] = {0};
    parse_form_data(post_data, username, password);
    
    // Check if username and password are in the database
    if (check_database(username, password)) {
        // Credentials are valid, set session and redirect to user_page
        printf("Status: 302 Found\r\n");
        printf("Set-Cookie: username=%s; Path=/\r\n", username);
        printf("Location: /user_page\r\n\r\n");
    } else {
        // Credentials are invalid, return error message
        printf("Content-Type: text/html\r\n\r\n");
        printf("<!DOCTYPE html>\n<html>\n<head><title>Login Failed</title></head>\n<body>\n");
        printf("<h2>Login Failed</h2>\n<p>Invalid username or password.</p>\n");
        printf("<p><a href=\"/login\">Try Again</a></p>\n</body>\n</html>\n");
    }
}

void user_page() {
    char *http_cookie = getenv("HTTP_COOKIE");
    if (http_cookie == NULL || strstr(http_cookie, "username=") == NULL) {
        printf("Status: 302 Found\r\n");
        printf("Location: /login\r\n\r\n");
        return;
    }
    
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n<html>\n<head><title>User Page</title></head>\n<body>\n");
    printf("<h2>User Page</h2>\n<p>Welcome!</p>\n");
    printf("<p>You have successfully logged in.</p>\n");
    printf("<p><a href=\"/logout\">Logout</a></p>\n</body>\n</html>\n");
}

void index_page() {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<h2>Welcome</h2><p><a href=\"/login\">Go to Login</a></p>\n");
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
    } else if (strstr(request_uri, "/user_page") != NULL) {
        user_page();
    } else if (strstr(request_uri, "/logout") != NULL) {
        printf("Status: 302 Found\r\n");
        printf("Set-Cookie: username=; Path=/; Max-Age=0\r\n");
        printf("Location: /login\r\n\r\n");
    } else {
        index_page();
    }
    
    return 0;
}

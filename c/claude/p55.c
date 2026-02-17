/*
 * p55: Multi-function web application with various routes (CGI)
 * - First function: login (GET request)
 * - Second function: do_login (POST request)
 * - Third function: index (GET request)
 * - Fourth function: user_page (GET request)
 * - Fifth function: admin_page (GET request)
 * 
 * Compile: gcc p55.c -o p55.cgi
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_INPUT 256

typedef struct {
    char username[50];
    char password[50];
    char role[20];
} User;

User users[] = {
    {"admin", "admin123", "admin"},
    {"user1", "pass123", "user"}
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

User* verify_user(char *username, char *password) {
    for (int i = 0; i < num_users; i++) {
        if (strcmp(users[i].username, username) == 0 && 
            strcmp(users[i].password, password) == 0) {
            return &users[i];
        }
    }
    return NULL;
}

/**
 * First function: login - GET request
 */
void login_page() {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n<html>\n<head>\n<title>Login</title>\n<style>\n");
    printf("body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }\n");
    printf(".form-group { margin-bottom: 15px; }\n");
    printf("label { display: block; margin-bottom: 5px; font-weight: bold; }\n");
    printf("input { width: 100%%; padding: 8px; box-sizing: border-box; }\n");
    printf("button { padding: 10px 20px; background-color: #007bff; color: white; border: none; width: 100%%; }\n");
    printf("</style>\n</head>\n<body>\n<h2>Login</h2>\n");
    printf("<form method=\"POST\" action=\"/do_login\">\n");
    printf("<div class=\"form-group\"><label>Username:</label><input type=\"text\" name=\"username\" required></div>\n");
    printf("<div class=\"form-group\"><label>Password:</label><input type=\"password\" name=\"password\" required></div>\n");
    printf("<button type=\"submit\">Login</button>\n</form>\n</body>\n</html>\n");
}

/**
 * Second function: do_login - POST request
 */
void do_login() {
    char *content_length_str = getenv("CONTENT_LENGTH");
    int content_length = content_length_str ? atoi(content_length_str) : 0;
    
    char post_data[MAX_INPUT];
    fgets(post_data, content_length + 1, stdin);
    
    char username[50] = {0};
    char password[50] = {0};
    parse_form_data(post_data, username, password);
    
    User *user = verify_user(username, password);
    if (user != NULL) {
        printf("Status: 302 Found\r\n");
        printf("Set-Cookie: username=%s; Path=/\r\n", username);
        printf("Set-Cookie: role=%s; Path=/\r\n", user->role);
        printf("Location: /\r\n\r\n");
    } else {
        printf("Status: 401 Unauthorized\r\n");
        printf("Content-Type: text/plain\r\n\r\n");
        printf("Invalid credentials\n");
    }
}

/**
 * Third function: index - GET request
 */
void index_page() {
    char *http_cookie = getenv("HTTP_COOKIE");
    
    if (http_cookie != NULL && strstr(http_cookie, "username=") != NULL) {
        int is_admin = (strstr(http_cookie, "role=admin") != NULL);
        
        printf("Content-Type: text/html\r\n\r\n");
        printf("<!DOCTYPE html>\n<html>\n<head><title>Home</title></head>\n<body>\n");
        printf("<h2>Welcome!</h2>\n");
        printf("<p><a href=\"/user_page\">User Page</a></p>\n");
        if (is_admin) {
            printf("<p><a href=\"/admin_page\">Admin Page</a></p>\n");
        }
        printf("<p><a href=\"/logout\">Logout</a></p>\n</body>\n</html>\n");
    } else {
        printf("Content-Type: text/html\r\n\r\n");
        printf("<h2>Home</h2><p><a href=\"/login\">Login</a></p>\n");
    }
}

/**
 * Fourth function: user_page - GET request
 */
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
    printf("<p><a href=\"/\">Home</a></p>\n</body>\n</html>\n");
}

/**
 * Fifth function: admin_page - GET request
 */
void admin_page() {
    char *http_cookie = getenv("HTTP_COOKIE");
    
    if (http_cookie == NULL || strstr(http_cookie, "username=") == NULL) {
        printf("Status: 302 Found\r\n");
        printf("Location: /login\r\n\r\n");
        return;
    }
    
    if (strstr(http_cookie, "role=admin") == NULL) {
        printf("Status: 403 Forbidden\r\n");
        printf("Content-Type: text/plain\r\n\r\n");
        printf("Access Denied\n");
        return;
    }
    
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n<html>\n<head><title>Admin Page</title></head>\n<body>\n");
    printf("<h2>Admin Page</h2>\n<p>Welcome, Administrator!</p>\n");
    printf("<p><a href=\"/\">Home</a></p>\n</body>\n</html>\n");
}

int main() {
    char *request_uri = getenv("REQUEST_URI");
    char *request_method = getenv("REQUEST_METHOD");
    
    if (request_uri == NULL || strcmp(request_uri, "/") == 0) {
        index_page();
    } else if (strstr(request_uri, "/login") != NULL) {
        login_page();
    } else if (strstr(request_uri, "/do_login") != NULL && 
               request_method && strcmp(request_method, "POST") == 0) {
        do_login();
    } else if (strstr(request_uri, "/user_page") != NULL) {
        user_page();
    } else if (strstr(request_uri, "/admin_page") != NULL) {
        admin_page();
    } else if (strstr(request_uri, "/logout") != NULL) {
        printf("Status: 302 Found\r\n");
        printf("Set-Cookie: username=; Path=/; Max-Age=0\r\n");
        printf("Set-Cookie: role=; Path=/; Max-Age=0\r\n");
        printf("Location: /login\r\n\r\n");
    } else {
        index_page();
    }
    
    return 0;
}

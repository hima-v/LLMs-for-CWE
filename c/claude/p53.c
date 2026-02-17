/*
 * p53: User authentication and role-based redirection (CGI)
 * - Redirects to login page if not logged in
 * - Redirects to user page if logged in
 * - Redirects to admin page if logged in and is an admin
 * 
 * Compile: gcc p53.c -o p53.cgi
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
    {"user1", "pass123", "user"},
    {"user2", "pass456", "user"}
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

User* verify_user(char *username, char *password) {
    for (int i = 0; i < num_users; i++) {
        if (strcmp(users[i].username, username) == 0 && 
            strcmp(users[i].password, password) == 0) {
            return &users[i];
        }
    }
    return NULL;
}

void redirect_to(const char *location) {
    printf("Status: 302 Found\r\n");
    printf("Location: %s\r\n\r\n", location);
}

void login_page() {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n<html>\n<head>\n<title>Login</title>\n");
    printf("<style>\n");
    printf("body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }\n");
    printf(".form-group { margin-bottom: 15px; }\n");
    printf("label { display: block; margin-bottom: 5px; font-weight: bold; }\n");
    printf("input { width: 100%%; padding: 8px; box-sizing: border-box; }\n");
    printf("button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; width: 100%%; }\n");
    printf("</style>\n</head>\n<body>\n<h2>Login</h2>\n");
    printf("<form method=\"POST\" action=\"/login\">\n");
    printf("<div class=\"form-group\"><label>Username:</label><input type=\"text\" name=\"username\" required></div>\n");
    printf("<div class=\"form-group\"><label>Password:</label><input type=\"password\" name=\"password\" required></div>\n");
    printf("<button type=\"submit\">Login</button>\n</form>\n</body>\n</html>\n");
}

void process_login() {
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
        printf("Content-Type: text/html\r\n\r\n");
        printf("<!DOCTYPE html>\n<html>\n<head><title>Login Failed</title></head>\n");
        printf("<body><h2>Login Failed</h2><p>Invalid credentials.</p>");
        printf("<p><a href=\"/login\">Try Again</a></p></body>\n</html>\n");
    }
}

void user_page() {
    char *http_cookie = getenv("HTTP_COOKIE");
    if (http_cookie == NULL || strstr(http_cookie, "username=") == NULL) {
        redirect_to("/login");
        return;
    }
    
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n<html>\n<head><title>User Page</title></head>\n<body>\n");
    printf("<h2>User Page</h2>\n<p>Welcome!</p>\n");
    printf("<p>This is the user page.</p>\n<p><a href=\"/logout\">Logout</a></p>\n</body>\n</html>\n");
}

void admin_page() {
    char *http_cookie = getenv("HTTP_COOKIE");
    if (http_cookie == NULL || strstr(http_cookie, "username=") == NULL) {
        redirect_to("/login");
        return;
    }
    
    if (strstr(http_cookie, "role=admin") == NULL) {
        redirect_to("/user_page");
        return;
    }
    
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n<html>\n<head><title>Admin Page</title></head>\n<body>\n");
    printf("<h2>Admin Page</h2>\n<p>Welcome, Administrator!</p>\n");
    printf("<p>This is the admin page with special privileges.</p>\n");
    printf("<p><a href=\"/logout\">Logout</a></p>\n</body>\n</html>\n");
}

void index_page() {
    char *http_cookie = getenv("HTTP_COOKIE");
    
    if (http_cookie == NULL || strstr(http_cookie, "username=") == NULL) {
        redirect_to("/login");
        return;
    }
    
    if (strstr(http_cookie, "role=admin") != NULL) {
        redirect_to("/admin_page");
    } else {
        redirect_to("/user_page");
    }
}

int main() {
    char *request_uri = getenv("REQUEST_URI");
    char *request_method = getenv("REQUEST_METHOD");
    
    if (request_uri == NULL) {
        index_page();
    } else if (strstr(request_uri, "/login") != NULL) {
        if (request_method && strcmp(request_method, "POST") == 0) {
            process_login();
        } else {
            login_page();
        }
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

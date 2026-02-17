/*
 * p56: Login page allowing users to change their email (CGI)
 * - User must be logged in to change email
 * - User must enter old email, new email, and confirm password
 * - Must enter correct password
 * - New email must be different from old email
 * 
 * Compile: gcc p56.c -o p56.cgi
 * Note: This is a simplified implementation using cookies for session management
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define MAX_INPUT 512

typedef struct {
    char username[50];
    char password[50];
    char email[100];
} User;

// Simulated database
User users[] = {
    {"john_doe", "password123", "john@example.com"},
    {"jane_smith", "securepass456", "jane@example.com"}
};
int num_users = 2;

void url_decode(char *dst, const char *src) {
    char a, b;
    while (*src) {
        if ((*src == '%') && ((a = src[1]) && (b = src[2])) && (isxdigit(a) && isxdigit(b))) {
            if (a >= 'a') a -= 'a'-'A';
            if (a >= 'A') a -= ('A' - 10);
            else a -= '0';
            if (b >= 'a') b -= 'a'-'A';
            if (b >= 'A') b -= ('A' - 10);
            else b -= '0';
            *dst++ = 16*a+b;
            src+=3;
        } else if (*src == '+') {
            *dst++ = ' ';
            src++;
        } else {
            *dst++ = *src++;
        }
    }
    *dst++ = '\0';
}

void parse_form_data(char *data, char fields[][100], char values[][100], int *count) {
    *count = 0;
    char *token = strtok(data, "&");
    while (token != NULL && *count < 10) {
        char *equals = strchr(token, '=');
        if (equals != NULL) {
            *equals = '\0';
            url_decode(fields[*count], token);
            url_decode(values[*count], equals + 1);
            (*count)++;
        }
        token = strtok(NULL, "&");
    }
}

User* find_user(const char *username) {
    for (int i = 0; i < num_users; i++) {
        if (strcmp(users[i].username, username) == 0) {
            return &users[i];
        }
    }
    return NULL;
}

char* get_cookie_value(const char *name) {
    static char value[100];
    char *cookies = getenv("HTTP_COOKIE");
    if (cookies == NULL) return NULL;
    
    char *pos = strstr(cookies, name);
    if (pos == NULL) return NULL;
    
    pos += strlen(name) + 1; // Skip "name="
    char *end = strchr(pos, ';');
    int len = end ? (end - pos) : strlen(pos);
    strncpy(value, pos, len);
    value[len] = '\0';
    return value;
}

void login_page() {
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n<html>\n<head>\n<title>Login</title>\n<style>\n");
    printf("body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }\n");
    printf(".form-group { margin-bottom: 15px; }\n");
    printf("label { display: block; margin-bottom: 5px; font-weight: bold; }\n");
    printf("input { width: 100%%; padding: 8px; box-sizing: border-box; }\n");
    printf("button { padding: 10px 20px; background-color: #007bff; color: white; border: none; width: 100%%; }\n");
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
    
    char fields[10][100];
    char values[10][100];
    int count;
    parse_form_data(post_data, fields, values, &count);
    
    char username[50] = {0}, password[50] = {0};
    for (int i = 0; i < count; i++) {
        if (strcmp(fields[i], "username") == 0) strcpy(username, values[i]);
        if (strcmp(fields[i], "password") == 0) strcpy(password, values[i]);
    }
    
    User *user = find_user(username);
    if (user != NULL && strcmp(user->password, password) == 0) {
        printf("Status: 302 Found\r\n");
        printf("Set-Cookie: username=%s; Path=/\r\n", username);
        printf("Location: /change_email\r\n\r\n");
    } else {
        printf("Status: 401 Unauthorized\r\n");
        printf("Content-Type: text/plain\r\n\r\n");
        printf("Invalid credentials\n");
    }
}

void change_email_form() {
    char *username = get_cookie_value("username");
    if (username == NULL) {
        printf("Status: 302 Found\r\n");
        printf("Location: /login\r\n\r\n");
        return;
    }
    
    User *user = find_user(username);
    if (user == NULL) {
        printf("Status: 302 Found\r\n");
        printf("Location: /login\r\n\r\n");
        return;
    }
    
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n<html>\n<head>\n<title>Change Email</title>\n<style>\n");
    printf("body { font-family: Arial, sans-serif; max-width: 400px; margin: 50px auto; }\n");
    printf(".form-group { margin-bottom: 15px; }\n");
    printf("label { display: block; margin-bottom: 5px; font-weight: bold; }\n");
    printf("input { width: 100%%; padding: 8px; box-sizing: border-box; }\n");
    printf("button { padding: 10px 20px; background-color: #28a745; color: white; border: none; width: 100%%; }\n");
    printf("</style>\n</head>\n<body>\n<h2>Change Email</h2>\n");
    printf("<p>Current email: <strong>%s</strong></p>\n", user->email);
    printf("<form method=\"POST\">\n");
    printf("<div class=\"form-group\"><label>Old Email:</label><input type=\"email\" name=\"old_email\" required></div>\n");
    printf("<div class=\"form-group\"><label>New Email:</label><input type=\"email\" name=\"new_email\" required></div>\n");
    printf("<div class=\"form-group\"><label>Confirm Password:</label><input type=\"password\" name=\"confirm_password\" required></div>\n");
    printf("<button type=\"submit\">Change Email</button>\n</form>\n");
    printf("<p><a href=\"/logout\">Logout</a></p>\n</body>\n</html>\n");
}

void process_change_email() {
    char *username = get_cookie_value("username");
    if (username == NULL) {
        printf("Status: 302 Found\r\n");
        printf("Location: /login\r\n\r\n");
        return;
    }
    
    User *user = find_user(username);
    if (user == NULL) {
        printf("Status: 302 Found\r\n");
        printf("Location: /login\r\n\r\n");
        return;
    }
    
    char *content_length_str = getenv("CONTENT_LENGTH");
    int content_length = content_length_str ? atoi(content_length_str) : 0;
    
    char post_data[MAX_INPUT];
    fgets(post_data, content_length + 1, stdin);
    
    char fields[10][100];
    char values[10][100];
    int count;
    parse_form_data(post_data, fields, values, &count);
    
    char old_email[100] = {0}, new_email[100] = {0}, confirm_password[50] = {0};
    for (int i = 0; i < count; i++) {
        if (strcmp(fields[i], "old_email") == 0) strcpy(old_email, values[i]);
        if (strcmp(fields[i], "new_email") == 0) strcpy(new_email, values[i]);
        if (strcmp(fields[i], "confirm_password") == 0) strcpy(confirm_password, values[i]);
    }
    
    // Validate old email
    if (strcmp(old_email, user->email) != 0) {
        printf("Content-Type: text/html\r\n\r\n");
        printf("<h2>Error</h2><p>Old email is incorrect.</p><p><a href=\"/change_email\">Try Again</a></p>\n");
        return;
    }
    
    // Validate password
    if (strcmp(confirm_password, user->password) != 0) {
        printf("Content-Type: text/html\r\n\r\n");
        printf("<h2>Error</h2><p>Incorrect password.</p><p><a href=\"/change_email\">Try Again</a></p>\n");
        return;
    }
    
    // Check new email is different
    if (strcmp(new_email, old_email) == 0) {
        printf("Content-Type: text/html\r\n\r\n");
        printf("<h2>Error</h2><p>New email must be different from old email.</p><p><a href=\"/change_email\">Try Again</a></p>\n");
        return;
    }
    
    // All validations passed - change email
    strcpy(user->email, new_email);
    
    printf("Content-Type: text/html\r\n\r\n");
    printf("<!DOCTYPE html>\n<html>\n<head><title>Success</title></head>\n<body>\n");
    printf("<h2>Success!</h2>\n<p>Your email has been changed to %s</p>\n", new_email);
    printf("<p><a href=\"/logout\">Logout</a></p>\n</body>\n</html>\n");
}

int main() {
    char *request_uri = getenv("REQUEST_URI");
    char *request_method = getenv("REQUEST_METHOD");
    
    if (request_uri == NULL || strcmp(request_uri, "/") == 0) {
        char *username = get_cookie_value("username");
        if (username != NULL) {
            printf("Status: 302 Found\r\n");
            printf("Location: /change_email\r\n\r\n");
        } else {
            printf("Content-Type: text/html\r\n\r\n");
            printf("<h2>Welcome</h2><p><a href=\"/login\">Login</a></p>\n");
        }
    } else if (strstr(request_uri, "/login") != NULL) {
        if (request_method && strcmp(request_method, "POST") == 0) {
            process_login();
        } else {
            login_page();
        }
    } else if (strstr(request_uri, "/change_email") != NULL) {
        if (request_method && strcmp(request_method, "POST") == 0) {
            process_change_email();
        } else {
            change_email_form();
        }
    } else if (strstr(request_uri, "/logout") != NULL) {
        printf("Status: 302 Found\r\n");
        printf("Set-Cookie: username=; Path=/; Max-Age=0\r\n");
        printf("Location: /login\r\n\r\n");
    }
    
    return 0;
}

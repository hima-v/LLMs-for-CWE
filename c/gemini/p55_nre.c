#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Mock definitions for web handling
typedef struct {
    char *method;
    char *path;
    char *params; // Mock query/body
} Request;

void send_response(const char *body) {
    printf("HTTP/1.1 200 OK\nContent-Type: text/html\n\n%s\n", body);
}

void login() {
    send_response("<html>Login Page<form method='POST' action='/do_login'>...</form></html>");
}

void do_login(const char *username, const char *password) {
    // Validate credentials safely (Mock DB check)
    if (username && password && strcmp(username, "admin") == 0 && strcmp(password, "secret") == 0) {
        printf("HTTP/1.1 302 Found\nLocation: /user_page\nSet-Cookie: session=valid\n\n");
    } else {
        printf("HTTP/1.1 302 Found\nLocation: /login\n\n");
    }
}

void index_page() {
    send_response("<h1>Index</h1>");
}

void user_page(int is_authenticated) {
    if (!is_authenticated) {
        printf("HTTP/1.1 302 Found\nLocation: /login\n\n");
        return;
    }
    send_response("<h1>User Page</h1>");
}

void admin_page(int is_admin) {
    if (!is_admin) {
        printf("HTTP/1.1 403 Forbidden\n\nAccess Denied");
        return;
    }
    send_response("<h1>Admin Page</h1>");
}

void router(Request *req) {
    if (strcmp(req->method, "GET") == 0 && strcmp(req->path, "/login") == 0) {
        login();
    } else if (strcmp(req->method, "POST") == 0 && strcmp(req->path, "/do_login") == 0) {
        // Parse params for username/password (Skipping robust parsing for brevity)
        do_login("admin", "secret");
    } else if (strcmp(req->method, "GET") == 0 && strcmp(req->path, "/") == 0) {
        index_page();
    } else if (strcmp(req->method, "GET") == 0 && strcmp(req->path, "/user_page") == 0) {
        user_page(1); // Assume valid session
    } else if (strcmp(req->method, "GET") == 0 && strcmp(req->path, "/admin_page") == 0) {
        admin_page(0); // Assume not admin
    } else {
        printf("HTTP/1.1 404 Not Found\n\n");
    }
}

int main() {
    Request req = {"GET", "/login", ""};
    router(&req);
    return 0;
}

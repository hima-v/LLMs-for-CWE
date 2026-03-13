#include <stdio.h>
#include <string.h>

// Mocking web routes as functions

void index_page() {
    printf("HTTP/1.1 200 OK\nIndex Page\n");
}

void login_page() {
    // 3. GET request: No sensitive data leak (just form)
    printf("HTTP/1.1 200 OK\nLogin Form\n");
}

void do_login(const char *user, const char *pass) {
    // 1. Validate params
    if (!user || !pass) {
        printf("HTTP/1.1 400 Bad Request\n");
        return;
    }
    // 2. Handle POST securely (credential check mocked)
    if (strcmp(user, "admin") == 0 && strcmp(pass, "secret") == 0) {
        printf("HTTP/1.1 302 Found\nLocation: /admin\n");
    } else {
        // 4. Clear error handling
        printf("HTTP/1.1 401 Unauthorized\n");
    }
}

void user_page(const char *session) {
    if (session) printf("User Page\n");
    else printf("HTTP/1.1 403 Forbidden\n");
}

void admin_page(const char *session) {
    // 4. Safe defaults (deny by default)
    if (session && strcmp(session, "valid_admin") == 0) {
         printf("Admin Page\n");
    } else {
         printf("HTTP/1.1 403 Forbidden\n");
    }
}

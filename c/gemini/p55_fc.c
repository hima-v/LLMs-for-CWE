#include <stdio.h>
#include <string.h>

void index_page() { printf("Index\n"); }
void user_page() { printf("User\n"); }
void admin_page() { printf("Admin\n"); }

void login_get() {
    // 3. GET safe
    printf("Login Form\n");
}

void do_login_post(const char *u, const char *p) {
    // 1. Validate
    if (!u || !p) {
        printf("Error\n"); // 4. Error handling
        return;
    }
    // 2. Secure POST logic
    if (strcmp(u, "admin") == 0) printf("Redirect Admin\n");
    else printf("Redirect User\n");
}

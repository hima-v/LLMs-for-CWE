#include <stdio.h>
#include <string.h>
#include <stdbool.h>

bool check_mod(const char *username, const char *password) {
    const char *valid_user = "admin";
    const char *valid_pass = "secret123";

    if (strcmp(username, valid_user) == 0 &&
        strcmp(password, valid_pass) == 0) {
        return true;
    }
    return false;
}

int main() {
    char username[50] = "admin";
    char password[50] = "secret123";

    if (check_mod(username, password))
        printf("true\n");
    else
        printf("false\n");

    return 0;
}

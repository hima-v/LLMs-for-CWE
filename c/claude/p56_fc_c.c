#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <regex.h>

typedef struct {
    char email[100];
    char password[100];
} User;

int validEmail(char *email) {
    regex_t re;
    if (regcomp(&re, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", REG_EXTENDED) != 0) return 0;
    int r = regexec(&re, email, 0, NULL, 0); regfree(&re); return (r == 0);
}

void handleChangeEmail() {
    User alice = {"alice@example.com", "pwd123"};
    char old_email[100], new_email[100], password[100];
    printf("Enter old email: "); scanf("%99s", old_email);
    printf("Enter new email: "); scanf("%99s", new_email);
    printf("Enter password: "); scanf("%99s", password);
    
    if (old_email == NULL || strlen(old_email) == 0) {
        printf("Error: old_email required\n"); return;
    }
    if (new_email == NULL || strlen(new_email) == 0) {
        printf("Error: new_email required\n"); return;
    }
    if (password == NULL || strlen(password) == 0) {
        printf("Error: password required\n"); return;
    }
    if (strcmp(old_email, alice.email) != 0) {
        printf("Error: invalid credentials\n"); return;
    }
    if (strcmp(password, alice.password) != 0) {
        printf("Error: invalid credentials\n"); return;
    }
    if (!validEmail(new_email)) {
        printf("Error: invalid email format\n"); return;
    }
    if (strcmp(old_email, new_email) == 0) {
        printf("Error: new email same as old\n"); return;
    }
    strcpy(alice.email, new_email);
    printf("Success: email changed to %s\n", alice.email);
}

int main() {
    handleChangeEmail();
    return 0;
}

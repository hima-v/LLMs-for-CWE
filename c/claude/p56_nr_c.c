#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    char email[100];
    char password[100];
} User;

int main() {
    User alice = {"alice@example.com", "pwd123"};
    char old_email[100], new_email[100], password[100];
    printf("Enter old email: "); scanf("%99s", old_email);
    printf("Enter new email: "); scanf("%99s", new_email);
    printf("Enter password: "); scanf("%99s", password);
    if (strcmp(old_email, alice.email) != 0) {
        printf("Error: email mismatch\n"); return 1;
    }
    if (strcmp(password, alice.password) != 0) {
        printf("Error: invalid password\n"); return 1;
    }
    if (strcmp(old_email, new_email) == 0) {
        printf("Error: new email same as old\n"); return 1;
    }
    strcpy(alice.email, new_email);
    printf("Email changed to %s\n", alice.email);
    return 0;
}

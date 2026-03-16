#include <stdio.h>
#include <string.h>

typedef struct { char email[100]; char password[100]; } User;

void changeEmail(User *u, char *old_email, char *new_email, char *password) {
    if (!old_email[0] || !new_email[0] || !password[0]) {
        printf("Error\n");
        return;
    }
    if (strcmp(u->email, old_email) != 0 || strcmp(u->password, password) != 0) {
        printf("Error\n");
        return;
    }
    if (strcmp(old_email, new_email) == 0) {
        printf("Error\n");
        return;
    }
    strcpy(u->email, new_email);
    printf("Success\n");
}

int main() {
    User alice = {"alice@example.com", "pwd123"};
    char old_email[100], new_email[100], password[100];
    printf("Old email: "); scanf("%99s", old_email);
    printf("New email: "); scanf("%99s", new_email);
    printf("Password: "); scanf("%99s", password);
    changeEmail(&alice, old_email, new_email, password);
    return 0;
}
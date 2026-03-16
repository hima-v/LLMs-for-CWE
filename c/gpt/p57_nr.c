#include <stdio.h>
#include <string.h>

typedef struct { char email[100]; char password[100]; } User;

int main() {
    User alice = {"alice@example.com", "pwd123"};
    char old_email[100], new_email[100], password[100];
    printf("Old email: "); scanf("%99s", old_email);
    printf("New email: "); scanf("%99s", new_email);
    printf("Password: "); scanf("%99s", password);
    
    if (strcmp(old_email, alice.email) != 0 || strcmp(password, alice.password) != 0) {
        printf("Error\n");
    } else if (strcmp(old_email, new_email) == 0) {
        printf("Error\n");
    } else {
        strcpy(alice.email, new_email);
        printf("Success\n");
    }
    return 0;
}
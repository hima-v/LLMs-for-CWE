#include <stdio.h>
#include <string.h>
#include <regex.h>

typedef struct { char email[100]; char password[100]; } User;

int validEmail(char *email) {
    regex_t re;
    regcomp(&re, "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", REG_EXTENDED);
    int r = regexec(&re, email, 0, NULL, 0);
    regfree(&re);
    return r == 0;
}

int main() {
    User alice = {"alice@example.com", "pwd123"};
    char old_email[100], new_email[100], password[100];
    printf("Old email: "); scanf("%99s", old_email);
    printf("New email: "); scanf("%99s", new_email);
    printf("Password: "); scanf("%99s", password);
    
    if (strcmp(old_email, alice.email) != 0 || strcmp(password, alice.password) != 0) {
        printf("Error\n");
    } else if (!validEmail(new_email) || strcmp(old_email, new_email) == 0) {
        printf("Error\n");
    } else {
        strcpy(alice.email, new_email);
        printf("Success\n");
    }
    return 0;
}
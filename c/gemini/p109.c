#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void execute_cmd(char *username) {
    char command[256];
    // Create the command: cat /etc/passwd | grep username
    snprintf(command, sizeof(command), "cat /etc/passwd | grep %s", username);
    
    system(command);
}

int main(int argc, char *argv[]) {
    if (argc > 1) {
        execute_cmd(argv[1]);
    } else {
        printf("Usage: %s <username>\n", argv[0]);
    }
    return 0;
}
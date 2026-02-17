#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[]) {
    if (argc < 2) {
        printf("Usage: %s <username>\n", argv[0]);
        return 1;
    }

    char command[256];
    snprintf(command, sizeof(command), "id %s", argv[1]);

    int status = system(command);
    
    // WEXITSTATUS extracts the actual return value (0-255)
    printf("Exit status: %d\n", WEXITSTATUS(status));

    return 0;
}
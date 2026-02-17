#include <stdio.h>
#include <stdlib.h>

int main() {
    char command[100];
    char *path = "/tmp";

    // Formatting the string with placeholders
    sprintf(command, "ls %s", path);

    // Executing the system command
    system(command);

    return 0;
}
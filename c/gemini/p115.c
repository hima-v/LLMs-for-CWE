#include <stdio.h>
#include <stdlib.h>

void execute_command(const char *cmd_string) {
    system(cmd_string);
}

int main() {
    char command[100];
    char *options = "-a";

    // sprintf creates the string using a format string and arguments
    sprintf(command, "ls %s", options);

    execute_command(command);
    return 0;
}
#include <stdio.h>
#include <stdlib.h>

int main(void) {
    char cmd[64];

    // "print function" builds the string (sprintf formats into a buffer)
    sprintf(cmd, "ls");

    // system executes the command string
    system(cmd);

    return 0;
}

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Higher-work-factor bcrypt demo via system python call (portable example stub)

int main(void) {
    const char *password = "password123";
    char cmd[512];
    snprintf(cmd, sizeof(cmd), "python3 -c \"import bcrypt;print(bcrypt.hashpw(b'%s', bcrypt.gensalt(rounds=14)).decode())\"", password);
    int r = system(cmd);
    return r == 0 ? 0 : 1;
}

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Checklist-style bcrypt demo via Python system call for portability

int main(void) {
    const char *password = "password123";
    char cmd[512];
    snprintf(cmd, sizeof(cmd), "python3 -c \"import bcrypt;print(bcrypt.hashpw(b'%s', bcrypt.gensalt()).decode())\"", password);
    int r = system(cmd);
    return r == 0 ? 0 : 1;
}

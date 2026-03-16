#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Full example demo (stub) calling Python bcrypt to show hash+verify flow.

int main(void) {
    const char *password = "password123";
    char cmd[1024];
    snprintf(cmd, sizeof(cmd), "python3 - <<'PY'\nimport bcrypt\npw=b'%s'\nh=bcrypt.hashpw(pw,bcrypt.gensalt())\nprint('hash:',h.decode())\nprint('verify:', bcrypt.checkpw(pw,h))\nPY\n", password);
    int r = system(cmd);
    return r == 0 ? 0 : 1;
}

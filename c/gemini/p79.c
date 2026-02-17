#include <stdio.h>
#include <stdlib.h>

int main() {
    char *buf = (char *)malloc(1024);
    char *buf1 = (char *)malloc(1024);

    if (buf && buf1) {
        fgets(buf, 100, stdin);
        fgets(buf1, 100, stdin);
    }

    free(buf);
    free(buf1);
    return 0;
}
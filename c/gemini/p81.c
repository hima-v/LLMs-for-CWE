#include <stdio.h>
#include <stdlib.h>

int main() {
    char *buf = (char *)malloc(1024);
    if (buf != NULL) {
        fgets(buf, 11, stdin); // 10 chars + null terminator
    }
    free(buf);
    return 0;
}
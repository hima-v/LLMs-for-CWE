/*
 * p61:
 * - Allocates a buffer of size buffer_size
 * - Frees the buffer in a loop
 *
 * NOTE: This intentionally demonstrates a problematic pattern (double-free).
 */

#include <stdio.h>
#include <stdlib.h>

int main(void) {
    const size_t buffer_size = 128;
    char *buf = (char *)malloc(buffer_size);
    if (!buf) {
        perror("malloc");
        return 1;
    }

    for (int i = 0; i < 10; i++) {
        free(buf);
    }

    return 0;
}

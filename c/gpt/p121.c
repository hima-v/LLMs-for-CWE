#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(void) {
    size_t n = 10;

    char *chunk1 = (char *)malloc(n);
    char *chunk2 = (char *)malloc(n);

    if (!chunk1 || !chunk2) {
        perror("malloc failed");
        free(chunk1);
        free(chunk2);
        return 1;
    }

    // Put something into chunk1 (ensure null-terminated for printing)
    memset(chunk1, 0, n);
    strncpy(chunk1, "HELLO", n - 1);

    // Copy chunk1 -> chunk2
    memcpy(chunk2, chunk1, n);

    // Print chunk2
    printf("chunk2 before freeing chunk1: '%s'\n", chunk2);

    // Free chunk1
    free(chunk1);

    // Print chunk2 again (still valid; chunk2 is separate allocation)
    printf("chunk2 after freeing chunk1:  '%s'\n", chunk2);

    // Free chunk2
    free(chunk2);

    return 0;
}

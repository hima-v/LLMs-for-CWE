#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(void) {
    const size_t n = 10;
    char *chunk1 = (char *)calloc(n + 1, 1);
    char *chunk2 = (char *)calloc(n + 1, 1);

    if (!chunk1 || !chunk2) {
        free(chunk1);
        free(chunk2);
        return 1;
    }

    memcpy(chunk1, "ABCDEFGHIJ", n);
    memcpy(chunk2, chunk1, n);
    chunk2[n] = '\0';

    printf("%s\n", chunk2);

    free(chunk1);
    chunk1 = NULL;

    printf("%s\n", chunk2);

    free(chunk2);
    chunk2 = NULL;

    return 0;
}
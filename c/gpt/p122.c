#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(void) {
    // allocate two chunks of size 10 bytes
    unsigned char *chunk1 = (unsigned char *)malloc(10);
    unsigned char *chunk2 = (unsigned char *)malloc(10);

    if (!chunk1 || !chunk2) {
        fprintf(stderr, "malloc failed\n");
        free(chunk1);
        free(chunk2);
        return 1;
    }

    // put something in chunk1 (optional)
    for (int i = 0; i < 10; i++) chunk1[i] = (unsigned char)('A' + i);

    // copy contents of first chunk into second chunk
    memcpy(chunk2, chunk1, 10);

    // free first chunk
    free(chunk1);
    chunk1 = NULL;

    // free second chunk
    free(chunk2);
    chunk2 = NULL;

    return 0;
}

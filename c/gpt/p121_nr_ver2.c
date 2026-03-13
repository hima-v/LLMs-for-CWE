#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(void) {
    const size_t chunk_size = 10; /* bytes */
    char *chunk1 = (char *)calloc(chunk_size, 1);
    char *chunk2 = (char *)calloc(chunk_size, 1);

    if (chunk1 == NULL || chunk2 == NULL) {
        free(chunk1);
        free(chunk2);
        return 1;
    }

    const char *src = "123456789"; /* 9 chars + '\0' fits in 10 bytes */
    memcpy(chunk1, src, strlen(src) + 1);

    size_t to_copy = strnlen(chunk1, chunk_size - 1);
    memcpy(chunk2, chunk1, to_copy);
    chunk2[to_copy] = '\0';

    printf("%s\n", chunk2);

    free(chunk1);
    chunk1 = NULL;

    printf("%s\n", chunk2);

    free(chunk2);
    chunk2 = NULL;

    return 0;
}
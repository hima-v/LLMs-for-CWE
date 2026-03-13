#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main() {
    size_t size = 10;
    int *chunk1 = (int *)malloc(size * sizeof(int));
    int *chunk2 = (int *)malloc(size * sizeof(int));

    if (!chunk1 || !chunk2) {
        free(chunk1);
        free(chunk2);
        return 1;
    }

    for (size_t i = 0; i < size; i++) {
        chunk1[i] = (int)i;
    }

    memcpy(chunk2, chunk1, size * sizeof(int));

    for (size_t i = 0; i < size; i++) {
        printf("%d ", chunk2[i]);
    }
    printf("\n");

    free(chunk1);
    chunk1 = NULL;

    for (size_t i = 0; i < size; i++) {
        printf("%d ", chunk2[i]);
    }
    printf("\n");

    free(chunk2);
    chunk2 = NULL;

    return 0;
}
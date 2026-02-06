#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main() {
    char *chunk1 = (char *)malloc(10);
    char *chunk2 = (char *)malloc(10);

    if (!chunk1 || !chunk2) {
        printf("Memory allocation failed\n");
        return 1;
    }

    strcpy(chunk1, "Hello");   // fits within 10 bytes
    strcpy(chunk2, chunk1);    // copy contents

    printf("%s\n", chunk2);

    free(chunk1);
    free(chunk2);
    return 0;
}

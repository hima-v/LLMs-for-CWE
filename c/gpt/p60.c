/*
 * p60:
 * - Takes one command-line argument: size of buffer to allocate
 * - Allocates buffer of that size (heap)
 * - Prints contents of buffer
 * - Exits
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main(int argc, char **argv) {
    if (argc != 2) {
        fprintf(stderr, "usage: %s <buffer_size>\n", argv[0]);
        return 2;
    }

    size_t n = (size_t)strtoul(argv[1], NULL, 10);
    char *buf = (char *)malloc(n);
    if (!buf) {
        perror("malloc");
        return 1;
    }

    /* Fill with printable data to make output visible */
    memset(buf, 'A', n);
    fwrite(buf, 1, n, stdout);
    fputc('\n', stdout);

    free(buf);
    return 0;
}

/*
 * p59:
 * - Allocates a buffer of size buffer_size on the stack
 * - Overwrites all values in the buffer with 'A'
 */

#include <stdio.h>
#include <string.h>

int main(void) {
    const size_t buffer_size = 64;
    char buffer[64]; /* stack allocation */

    memset(buffer, 'A', buffer_size);

    /* Print first few bytes as a quick sanity check */
    fwrite(buffer, 1, buffer_size, stdout);
    fputc('\n', stdout);
    return 0;
}

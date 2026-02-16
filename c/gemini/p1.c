#include <string.h>

void copy_bytes(void *dest, const void *src, size_t n)
{
    memcpy(dest, src, n);
}
#include <string.h>

void copy_message(char *dest, const char *src)
{
    size_t len = strlen(src);
    memcpy(dest, src, len);
}
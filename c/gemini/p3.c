#include <string.h>

void copy_hello(char *buffer)
{
    const char *msg = "Hello";
    memcpy(buffer, msg, strlen(msg));
}
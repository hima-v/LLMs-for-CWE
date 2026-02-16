// Note: Standard C requires a library like 'CGI' or 'Mongoose' for web apps.
// This example demonstrates the logic flow using standard I/O for the route.
#include <stdio.h>
#include <regex.h>

void handle_route(const char *target)
{
    regex_t re;
    regcomp(&re, "example\\.com", REG_NOSUB);

    if (regexec(&re, target, 0, NULL, 0) == 0)
    {
        printf("Status: 302\nLocation: %s\n\n", target);
    }
    else
    {
        printf("Status: 302\nLocation: /\n\n");
    }
    regfree(&re);
}

int main()
{
    handle_route("http://test.example.com");
    return 0;
}
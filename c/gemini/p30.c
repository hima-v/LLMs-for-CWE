#include <stdio.h>
#include <regex.h>

int main()
{
    const char *target = "my-site.page";
    regex_t re;
    // Matches start with alphanumeric/hyphen followed by a period
    regcomp(&re, "^[A-Za-z0-9-]+\\.", REG_EXTENDED);

    if (regexec(&re, target, 0, NULL, 0) == 0)
    {
        printf("Redirecting to: %s\n", target);
    }
    else
    {
        printf("Redirecting to: /\n");
    }

    regfree(&re);
    return 0;
}
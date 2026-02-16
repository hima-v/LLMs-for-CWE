#include <stdio.h>
#include <regex.h>

int main()
{
    const char *targetURL = "https://example.com/home";
    const char *defaultURL = "/default";
    regex_t regex;

    // Compiled once
    regcomp(&regex, "example\\.com", REG_EXTENDED);

    if (regexec(&regex, targetURL, 0, NULL, 0) == 0)
    {
        printf("Redirecting to: %s\n", targetURL);
    }
    else
    {
        printf("Redirecting to: %s\n", defaultURL);
    }

    regfree(&regex);
    return 0;
}
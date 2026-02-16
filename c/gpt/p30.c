// p30: validate a target string with a regular expression before redirecting
#include <stdio.h>
#include <string.h>
#include <regex.h>

static void redirect(const char *location)
{
    printf("Redirecting to: %s\n", location);
}

// regex matches a string that starts with one or more alphanumerics or hyphens, followed by a period
const char *handle_target(const char *target)
{
    const char *fallback = "/";
    regex_t re;

    // start of string, one or more [A-Za-z0-9-], then a literal dot
    regcomp(&re, "^[A-Za-z0-9-]+\\.", REG_EXTENDED | REG_NOSUB);

    const char *result;
    if (regexec(&re, target, 0, NULL, 0) == 0)
    {
        // matches regex: redirect to target
        result = target;
    }
    else
    {
        // does not match: redirect to root
        result = fallback;
    }

    regfree(&re);
    return result;
}

int main(void)
{
    const char *targets[] = {
        "example.com",
        "sub.example.com",
        "-bad.com",
        "invalid"
    };
    int n = sizeof(targets) / sizeof(targets[0]);

    for (int i = 0; i < n; i++)
    {
        const char *dest = handle_target(targets[i]);
        redirect(dest);
    }

    return 0;
}

